package com.tafhdev.split_bill_app.payment.service.dto.result;

import java.util.UUID;

public record ParticipantResult(

        UUID id,
        String name
) {
}
