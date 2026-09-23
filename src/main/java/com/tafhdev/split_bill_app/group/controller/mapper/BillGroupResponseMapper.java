package com.tafhdev.split_bill_app.group.controller.mapper;

import com.tafhdev.split_bill_app.group.controller.dto.BillGroupResponse;
import com.tafhdev.split_bill_app.group.controller.dto.ParticipantResponse;
import com.tafhdev.split_bill_app.group.domain.BillGroup;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BillGroupResponseMapper {

    public BillGroupResponse toResponse(
            BillGroup billGroup
    ) {
        List<ParticipantResponse> participants = billGroup.getParticipants().stream()
                .map(participant ->
                        new ParticipantResponse(
                                participant.getId(),
                                participant.getName()
                        )
                )
                .toList();

        return new BillGroupResponse(
                billGroup.getId(),
                billGroup.getName(),
                participants,
                billGroup.getCreatedAt()
        );
    }
}
