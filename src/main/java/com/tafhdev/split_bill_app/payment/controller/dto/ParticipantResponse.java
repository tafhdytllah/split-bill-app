package com.tafhdev.split_bill_app.payment.controller.dto;

import java.util.UUID;

public record ParticipantResponse(

        UUID id,

        String name
) {
}
