package com.tafhdev.split_bill_app.payment.controller;


import com.tafhdev.split_bill_app.payment.controller.dto.request.CreatePaymentRequest;
import com.tafhdev.split_bill_app.payment.controller.dto.response.PaymentResponse;
import com.tafhdev.split_bill_app.payment.controller.mapper.PaymentApiMapper;
import com.tafhdev.split_bill_app.payment.service.PaymentService;
import com.tafhdev.split_bill_app.payment.service.dto.command.CreatePaymentCommand;
import com.tafhdev.split_bill_app.payment.service.dto.result.PaymentResult;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/groups/{groupId}/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentApiMapper paymentApiMapper;

    public PaymentController(
            PaymentService paymentService,
            PaymentApiMapper paymentApiMapper
    ) {
        this.paymentService = paymentService;
        this.paymentApiMapper = paymentApiMapper;
    }

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(
            @PathVariable UUID groupId,
            @Valid @RequestBody CreatePaymentRequest request
    ) {
        CreatePaymentCommand command = paymentApiMapper.toCommand(
                groupId,
                request
        );

        PaymentResult result = paymentService.createPayment(command);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(paymentApiMapper.toResponse(result));
    }
}
