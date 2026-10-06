package com.tafhdev.split_bill_app.group.service.dto;

import com.tafhdev.split_bill_app.group.controller.dto.BillGroupResponse;

public record BillGroupResult(

        BillGroupResponse response,

        boolean replay
) {
}
