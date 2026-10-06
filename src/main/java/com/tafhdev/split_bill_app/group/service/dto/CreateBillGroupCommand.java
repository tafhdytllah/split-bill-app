package com.tafhdev.split_bill_app.group.service.dto;

import java.util.List;

public record CreateBillGroupCommand(

        String idempotencyKey,

        String name,

        List<String> participantNames
) {
}
