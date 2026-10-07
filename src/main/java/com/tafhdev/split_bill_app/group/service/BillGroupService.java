package com.tafhdev.split_bill_app.group.service;

import com.tafhdev.split_bill_app.group.controller.dto.BillGroupResponse;
import com.tafhdev.split_bill_app.group.controller.mapper.BillGroupApiMapper;
import com.tafhdev.split_bill_app.group.domain.BillGroup;
import com.tafhdev.split_bill_app.group.domain.Participant;
import com.tafhdev.split_bill_app.group.repository.BillGroupRepository;
import com.tafhdev.split_bill_app.group.service.dto.BillGroupResult;
import com.tafhdev.split_bill_app.group.service.dto.CreateBillGroupCommand;
import com.tafhdev.split_bill_app.shared.application.exception.ConflictException;
import com.tafhdev.split_bill_app.shared.application.exception.ResourceNotFoundException;
import com.tafhdev.split_bill_app.shared.application.service.IdempotencyHashGenerator;
import com.tafhdev.split_bill_app.shared.application.service.IdempotencyRequestBuilder;
import com.tafhdev.split_bill_app.shared.application.service.IdempotencyService;
import com.tafhdev.split_bill_app.shared.domain.Idempotency;
import com.tafhdev.split_bill_app.shared.domain.IdempotencyScope;
import com.tafhdev.split_bill_app.shared.infrastructure.generator.IdGenerator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class BillGroupService {

    private final BillGroupRepository billGroupRepository;
    private final IdempotencyHashGenerator idempotencyHashGenerator;
    private final IdempotencyService idempotencyService;
    private final IdGenerator idGenerator;
    private final Clock clock;
    private final BillGroupApiMapper billGroupApiMapper;

    public BillGroupService(
            BillGroupRepository billGroupRepository,
            IdempotencyHashGenerator idempotencyHashGenerator,
            IdempotencyService idempotencyService,
            IdGenerator idGenerator,
            Clock clock,
            BillGroupApiMapper billGroupApiMapper
    ) {
        this.billGroupRepository = billGroupRepository;
        this.idempotencyHashGenerator = idempotencyHashGenerator;
        this.idempotencyService = idempotencyService;
        this.idGenerator = idGenerator;
        this.clock = clock;
        this.billGroupApiMapper = billGroupApiMapper;
    }

    @Transactional
    public BillGroupResult createGroup(CreateBillGroupCommand command) {

        String request = IdempotencyRequestBuilder.build(
                command.name(),
                command.participantNames()
        );

        String requestHash = idempotencyHashGenerator.generate(request);

        Idempotency idempotency = Idempotency.createNew(
                idGenerator.generate(),
                IdempotencyScope.GROUP,
                command.idempotencyKey(),
                requestHash,
                Instant.now(clock)
        );

        boolean idempotencyCreated = idempotencyService.insertIfAbsent(idempotency);

        if (!idempotencyCreated) {

            Idempotency existing = idempotencyService.find(
                    IdempotencyScope.GROUP,
                    command.idempotencyKey()
            ).orElseThrow(() ->
                    new IllegalStateException("idempotency record not found")
            );

                boolean sameHash = MessageDigest.isEqual(
                    existing.getRequestHash().getBytes(StandardCharsets.UTF_8),
                    requestHash.getBytes(StandardCharsets.UTF_8)
            );

            if (!sameHash) {
                throw new ConflictException(
                        "idempotency key reused with different request"
                );
            }

            BillGroupResponse response = idempotencyService.getResponse(
                    existing,
                    BillGroupResponse.class
            );

            return new BillGroupResult(
                    response,
                    true
            );
        }

        UUID groupId = idGenerator.generate();

        List<Participant> participants = command.participantNames().stream()
                .map(participantName ->
                        Participant.createNew(
                                idGenerator.generate(),
                                groupId,
                                participantName,
                                Instant.now(clock)
                        )
                )
                .toList();

        BillGroup group = BillGroup.createNew(
                groupId,
                command.name(),
                participants,
                Instant.now(clock)
        );

        BillGroup savedBillGroup = billGroupRepository.save(group);

        BillGroupResponse response = billGroupApiMapper.toResponse(savedBillGroup);

        idempotencyService.complete(
                idempotency,
                HttpStatus.CREATED.value(),
                response
        );

        return new BillGroupResult(
                response,
                false
        );
    }
}
