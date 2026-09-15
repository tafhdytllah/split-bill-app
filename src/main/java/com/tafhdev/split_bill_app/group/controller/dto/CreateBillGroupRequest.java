package com.tafhdev.split_bill_app.group.controller.dto;

import java.util.List;

public record CreateBillGroupRequest(
        String name,
        List<String> participants
) {
}
