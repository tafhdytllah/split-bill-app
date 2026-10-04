package com.tafhdev.split_bill_app.group.service;

import com.tafhdev.split_bill_app.group.controller.dto.BillGroupResponse;
import com.tafhdev.split_bill_app.group.controller.mapper.BillGroupApiMapper;
import com.tafhdev.split_bill_app.group.domain.BillGroup;
import com.tafhdev.split_bill_app.group.domain.Participant;
import com.tafhdev.split_bill_app.group.repository.BillGroupRepository;
import com.tafhdev.split_bill_app.group.service.dto.CreateBillGroupCommand;
import com.tafhdev.split_bill_app.shared.application.service.IdempotencyHashGenerator;
import com.tafhdev.split_bill_app.shared.application.service.IdempotencyRequestBuilder;
import com.tafhdev.split_bill_app.shared.application.service.IdempotencyService;
import com.tafhdev.split_bill_app.shared.domain.Idempotency;
import com.tafhdev.split_bill_app.shared.domain.IdempotencyScope;
import com.tafhdev.split_bill_app.shared.domain.exception.DomainException;
import com.tafhdev.split_bill_app.shared.infrastructure.generator.IdGenerator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

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
    private final ObjectMapper objectMapper;
    private final IdGenerator idGenerator;
    private final Clock clock;
    private final BillGroupApiMapper billGroupApiMapper;

    public BillGroupService(
            BillGroupRepository billGroupRepository,
            IdempotencyHashGenerator idempotencyHashGenerator,
            IdempotencyService idempotencyService,
            ObjectMapper objectMapper,
            IdGenerator idGenerator,
            Clock clock,
            BillGroupApiMapper billGroupApiMapper
    ) {
        this.billGroupRepository = billGroupRepository;
        this.idempotencyHashGenerator = idempotencyHashGenerator;
        this.idempotencyService = idempotencyService;
        this.objectMapper = objectMapper;
        this.idGenerator = idGenerator;
        this.clock = clock;
        this.billGroupApiMapper = billGroupApiMapper;
    }

    @Transactional
    public BillGroupResponse createGroup(CreateBillGroupCommand command) {

        String request = IdempotencyRequestBuilder.build(
                command.name(),
                command.participantNames()
        );

        String requestHash = idempotencyHashGenerator.generate(request);

        Optional<Idempotency> existing =
                idempotencyService.find(
                        IdempotencyScope.GROUP,
                        command.idempotencyKey()
                );

        if (existing.isPresent()) {
            Idempotency idempotency = existing.get();

            boolean sameHash = MessageDigest.isEqual(
                    idempotency.getRequestHash()
                            .getBytes(StandardCharsets.UTF_8),
                    requestHash.getBytes(StandardCharsets.UTF_8)
            );

            if (!sameHash) {
                throw new DomainException(
                        "idempotency key reused with different request"
                );
            }

            return idempotencyService.getResponse(
                    idempotency,
                    BillGroupResponse.class
            );
        }

        Idempotency idempotency =
                idempotencyService.create(
                        IdempotencyScope.GROUP,
                        command.idempotencyKey(),
                        requestHash
                );

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

        return response;
    }
}
