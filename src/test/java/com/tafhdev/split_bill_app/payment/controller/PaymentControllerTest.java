package com.tafhdev.split_bill_app.payment.controller;

import com.tafhdev.split_bill_app.payment.controller.dto.CreatePaymentRequest;
import com.tafhdev.split_bill_app.payment.controller.dto.ParticipantResponse;
import com.tafhdev.split_bill_app.payment.controller.dto.PaymentResponse;
import com.tafhdev.split_bill_app.payment.controller.mapper.PaymentApiMapper;
import com.tafhdev.split_bill_app.payment.service.PaymentService;
import com.tafhdev.split_bill_app.payment.service.dto.CreatePaymentCommand;
import com.tafhdev.split_bill_app.payment.service.dto.PaymentResult;
import com.tafhdev.split_bill_app.shared.domain.Money;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PaymentController.class)
@AutoConfigureMockMvc(addFilters = false)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PaymentService paymentService;

    @MockitoBean
    private PaymentApiMapper paymentApiMapper;

    @Test
    void shouldCreatePayment() throws Exception {

        UUID groupId = UUID.randomUUID();
        UUID fromParticipantId = UUID.randomUUID();
        UUID toParticipantId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();

        CreatePaymentRequest request = new CreatePaymentRequest(
                fromParticipantId,
                toParticipantId,
                new BigDecimal("50000.00")
        );

        CreatePaymentCommand command = new CreatePaymentCommand(
                "payment-key",
                groupId,
                fromParticipantId,
                toParticipantId,
                Money.of(new BigDecimal("50000.00"))
        );

        PaymentResponse response = new PaymentResponse(
                paymentId,
                groupId,
                new ParticipantResponse(
                        fromParticipantId,
                        "Taufik"
                ),
                new ParticipantResponse(
                        toParticipantId,
                        "Andi"
                ),
                new BigDecimal("50000.00"),
                Instant.parse("2026-01-01T10:00:00Z")
        );

        when(paymentApiMapper.toCommand(
                "payment-key",
                groupId,
                request
        )).thenReturn(command);

        when(paymentService.createPayment(command))
                .thenReturn(
                        new PaymentResult(
                                response,
                                false
                        )
                );

        mockMvc.perform(
                        post(
                                "/api/groups/{groupId}/payments",
                                groupId
                        )
                                .header(
                                        "Idempotency-Key",
                                        "payment-key"
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id")
                        .value(paymentId.toString()))
                .andExpect(jsonPath("$.data.groupId")
                        .value(groupId.toString()))
                .andExpect(jsonPath("$.data.fromParticipant.id")
                        .value(fromParticipantId.toString()))
                .andExpect(jsonPath("$.data.fromParticipant.name")
                        .value("Taufik"))
                .andExpect(jsonPath("$.data.toParticipant.id")
                        .value(toParticipantId.toString()))
                .andExpect(jsonPath("$.data.toParticipant.name")
                        .value("Andi"))
                .andExpect(jsonPath("$.data.amount")
                        .value("50000.00"))
                .andExpect(jsonPath("$.data.createdAt")
                        .value("2026-01-01T10:00:00Z"));

        verify(paymentApiMapper)
                .toCommand(
                        "payment-key",
                        groupId,
                        request
                );

        verify(paymentService)
                .createPayment(command);
    }

    @Test
    void shouldReturnOkWhenPaymentIsIdempotentReplay() throws Exception {

        UUID groupId = UUID.randomUUID();
        UUID fromParticipantId = UUID.randomUUID();
        UUID toParticipantId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();

        CreatePaymentRequest request = new CreatePaymentRequest(
                fromParticipantId,
                toParticipantId,
                new BigDecimal("50000.00")
        );

        CreatePaymentCommand command = new CreatePaymentCommand(
                "payment-key",
                groupId,
                fromParticipantId,
                toParticipantId,
                Money.of(new BigDecimal("50000.00"))
        );

        PaymentResponse response = new PaymentResponse(
                paymentId,
                groupId,
                new ParticipantResponse(
                        fromParticipantId,
                        "Taufik"
                ),
                new ParticipantResponse(
                        toParticipantId,
                        "Andi"
                ),
                new BigDecimal("50000.00"),
                Instant.parse("2026-01-01T10:00:00Z")
        );

        when(paymentApiMapper.toCommand(
                "payment-key",
                groupId,
                request
        )).thenReturn(command);

        when(paymentService.createPayment(command))
                .thenReturn(
                        new PaymentResult(
                                response,
                                true
                        )
                );

        mockMvc.perform(
                        post(
                                "/api/groups/{groupId}/payments",
                                groupId
                        )
                                .header(
                                        "Idempotency-Key",
                                        "payment-key"
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id")
                        .value(paymentId.toString()))
                .andExpect(jsonPath("$.data.groupId")
                        .value(groupId.toString()))
                .andExpect(jsonPath("$.data.fromParticipant.id")
                        .value(fromParticipantId.toString()))
                .andExpect(jsonPath("$.data.fromParticipant.name")
                        .value("Taufik"))
                .andExpect(jsonPath("$.data.toParticipant.id")
                        .value(toParticipantId.toString()))
                .andExpect(jsonPath("$.data.toParticipant.name")
                        .value("Andi"))
                .andExpect(jsonPath("$.data.amount")
                        .value("50000.00"));

        verify(paymentApiMapper)
                .toCommand(
                        "payment-key",
                        groupId,
                        request
                );

        verify(paymentService)
                .createPayment(command);
    }

    @Test
    void shouldReturnBadRequestWhenIdempotencyKeyIsMissing() throws Exception {

        UUID groupId = UUID.randomUUID();
        UUID fromParticipantId = UUID.randomUUID();
        UUID toParticipantId = UUID.randomUUID();

        CreatePaymentRequest request = new CreatePaymentRequest(
                fromParticipantId,
                toParticipantId,
                new BigDecimal("50000.00")
        );

        mockMvc.perform(
                        post(
                                "/api/groups/{groupId}/payments",
                                groupId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.code")
                        .value("BAD_REQUEST"))
                .andExpect(jsonPath("$.errors.message")
                        .value(
                                "Missing required header: Idempotency-Key"
                        ));
    }

    @Test
    void shouldReturnBadRequestWhenIdempotencyKeyIsBlank() throws Exception {

        UUID groupId = UUID.randomUUID();
        UUID fromParticipantId = UUID.randomUUID();
        UUID toParticipantId = UUID.randomUUID();

        CreatePaymentRequest request = new CreatePaymentRequest(
                fromParticipantId,
                toParticipantId,
                new BigDecimal("50000.00")
        );

        mockMvc.perform(
                        post(
                                "/api/groups/{groupId}/payments",
                                groupId
                        )
                                .header(
                                        "Idempotency-Key",
                                        "   "
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.code")
                        .value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors.message")
                        .value(
                                "Validation failed"
                        ));
    }

    @Test
    void shouldReturnBadRequestWhenRequestIsInvalid() throws Exception {

        UUID groupId = UUID.randomUUID();

        String request = """
                {
                    "fromParticipantId": null,
                    "toParticipantId": null,
                    "amount": 0
                }
                """;

        mockMvc.perform(
                        post(
                                "/api/groups/{groupId}/payments",
                                groupId
                        )
                                .header(
                                        "Idempotency-Key",
                                        "payment-key"
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.code")
                        .value("VALIDATION_ERROR"));
    }

    @Test
    void shouldReturnBadRequestWhenGroupIdIsInvalid() throws Exception {

        UUID fromParticipantId = UUID.randomUUID();
        UUID toParticipantId = UUID.randomUUID();

        CreatePaymentRequest request = new CreatePaymentRequest(
                fromParticipantId,
                toParticipantId,
                new BigDecimal("50000.00")
        );

        mockMvc.perform(
                        post(
                                "/api/groups/{groupId}/payments",
                                "invalid-uuid"
                        )
                                .header(
                                        "Idempotency-Key",
                                        "payment-key"
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.code")
                        .value("BAD_REQUEST"));
    }
}