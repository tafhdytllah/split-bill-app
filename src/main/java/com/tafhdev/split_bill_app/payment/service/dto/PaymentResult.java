package com.tafhdev.split_bill_app.payment.service.dto;

import com.tafhdev.split_bill_app.payment.controller.dto.PaymentResponse;

public record PaymentResult(

        PaymentResponse response,

        boolean replay
) {
}
