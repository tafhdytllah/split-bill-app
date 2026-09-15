package com.tafhdev.split_bill_app.group.controller.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record BillGroupResponse(
        UUID id,
        String name,
        List<ParticipantResponse> participants,
        Instant createdAt
) {
}
