package com.tafhdev.split_bill_app.payment.controller;

import com.tafhdev.split_bill_app.payment.controller.dto.CreatePaymentRequest;
import com.tafhdev.split_bill_app.payment.controller.dto.PaymentResponse;
import com.tafhdev.split_bill_app.payment.controller.mapper.PaymentApiMapper;
import com.tafhdev.split_bill_app.payment.service.PaymentService;
import com.tafhdev.split_bill_app.payment.service.dto.CreatePaymentCommand;
import com.tafhdev.split_bill_app.payment.service.dto.PaymentResult;
import com.tafhdev.split_bill_app.shared.infrastructure.web.response.ApiResponse;
import com.tafhdev.split_bill_app.shared.infrastructure.web.response.ResponseFactory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Validated
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
    public ResponseEntity<ApiResponse<PaymentResponse>> createPayment(
            @PathVariable
            UUID groupId,

            @RequestHeader("Idempotency-Key")
            @NotBlank(message = "Idempotency-Key must not be blank")
            String idempotencyKey,

            @Valid
            @RequestBody
            CreatePaymentRequest request
    ) {
        CreatePaymentCommand command = paymentApiMapper.toCommand(
                idempotencyKey,
                groupId,
                request
        );

        PaymentResult result = paymentService.createPayment(command);

        return result.reply()
                ? ResponseFactory.ok(result.response())
                : ResponseFactory.created(result.response());
    }
}
