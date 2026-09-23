package com.tafhdev.split_bill_app.group.service;

import com.tafhdev.split_bill_app.group.domain.BillGroup;
import com.tafhdev.split_bill_app.group.domain.Participant;
import com.tafhdev.split_bill_app.group.repository.BillGroupRepository;
import com.tafhdev.split_bill_app.group.service.dto.CreateBillGroupResult;
import com.tafhdev.split_bill_app.shared.infrastructure.generator.IdGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class BillGroupService {

    private final BillGroupRepository billGroupRepository;
    private final IdGenerator idGenerator;
    private final Clock clock;

    public BillGroupService(
            BillGroupRepository billGroupRepository,
            IdGenerator idGenerator,
            Clock clock
    ) {
        this.billGroupRepository = billGroupRepository;
        this.idGenerator = idGenerator;
        this.clock = clock;
    }

    @Transactional
    public CreateBillGroupResult createGroup(
            String name,
            List<String> participantNames
    ) {
        UUID groupId = idGenerator.generate();

        Instant now = Instant.now(clock);

        List<Participant> participants = participantNames.stream()
                .map(participantName ->
                        Participant.createNew(
                                idGenerator.generate(),
                                groupId,
                                participantName,
                                now
                        )
                )
                .toList();

        BillGroup group = BillGroup.createNew(
                groupId,
                name,
                participants,
                now
        );

        BillGroup savedBillGroup = billGroupRepository.save(group);

        return new CreateBillGroupResult(
                savedBillGroup
        );
    }
}
