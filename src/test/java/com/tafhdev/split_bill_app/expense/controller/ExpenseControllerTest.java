package com.tafhdev.split_bill_app.expense.controller;

import com.tafhdev.split_bill_app.expense.controller.dto.CreateExpenseRequest;
import com.tafhdev.split_bill_app.expense.controller.dto.ExpenseResponse;
import com.tafhdev.split_bill_app.expense.controller.dto.ExpenseSplitResponse;
import com.tafhdev.split_bill_app.expense.controller.dto.SplitParticipantRequest;
import com.tafhdev.split_bill_app.expense.controller.dto.SplitRequest;
import com.tafhdev.split_bill_app.expense.controller.mapper.ExpenseApiMapper;
import com.tafhdev.split_bill_app.expense.domain.ExpenseCategory;
import com.tafhdev.split_bill_app.expense.domain.ExpenseSplitType;
import com.tafhdev.split_bill_app.expense.service.ExpenseService;
import com.tafhdev.split_bill_app.expense.service.dto.CreateExpenseCommand;
import com.tafhdev.split_bill_app.expense.service.dto.ExpenseResult;
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
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(ExpenseController.class)
class ExpenseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ExpenseService expenseService;

    @MockitoBean
    private ExpenseApiMapper expenseApiMapper;

    @Test
    void createExpense_shouldReturn201Created_whenExpenseIsCreated() throws Exception {

        UUID groupId = UUID.randomUUID();
        UUID paidBy = UUID.randomUUID();
        UUID participant2 = UUID.randomUUID();

        String idempotencyKey = "expense-001";

        CreateExpenseRequest request = new CreateExpenseRequest(
                paidBy,
                new BigDecimal("100000"),
                ExpenseCategory.FOOD,
                new SplitRequest(
                        ExpenseSplitType.EQUAL,
                        List.of(
                                new SplitParticipantRequest(
                                        paidBy,
                                        null,
                                        null
                                ),
                                new SplitParticipantRequest(
                                        participant2,
                                        null,
                                        null
                                )
                        )
                )
        );

        CreateExpenseCommand command = new CreateExpenseCommand(
                idempotencyKey,
                groupId,
                paidBy,
                com.tafhdev.split_bill_app.shared.domain.Money.of(
                        new BigDecimal("100000")
                ),
                ExpenseCategory.FOOD,
                new com.tafhdev.split_bill_app.expense.service.dto.SplitCommand(
                        ExpenseSplitType.EQUAL,
                        List.of(
                                new com.tafhdev.split_bill_app.expense.service.dto.SplitParticipantCommand(
                                        paidBy,
                                        null,
                                        null
                                ),
                                new com.tafhdev.split_bill_app.expense.service.dto.SplitParticipantCommand(
                                        participant2,
                                        null,
                                        null
                                )
                        )
                )
        );

        ExpenseResponse response = new ExpenseResponse(
                UUID.randomUUID(),
                groupId,
                paidBy,
                new BigDecimal("100000"),
                ExpenseCategory.FOOD,
                ExpenseSplitType.EQUAL,
                List.of(
                        new ExpenseSplitResponse(
                                UUID.randomUUID(),
                                paidBy,
                                "Taufik",
                                new BigDecimal("50000")
                        ),
                        new ExpenseSplitResponse(
                                UUID.randomUUID(),
                                participant2,
                                "Participant 2",
                                new BigDecimal("50000")
                        )
                ),
                Instant.parse("2026-10-06T10:00:00Z")
        );

        when(expenseApiMapper.toCommand(
                eq(idempotencyKey),
                eq(groupId),
                eq(request)
        )).thenReturn(command);

        when(expenseService.createExpense(eq(command)))
                .thenReturn(new ExpenseResult(response, false));

        mockMvc.perform(
                        post("/api/groups/{groupId}/expenses", groupId)
                                .header("Idempotency-Key", idempotencyKey)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(response.id().toString()))
                .andExpect(jsonPath("$.data.groupId").value(groupId.toString()))
                .andExpect(jsonPath("$.data.paidBy").value(paidBy.toString()))
                .andExpect(jsonPath("$.data.amount").value("100000"))
                .andExpect(jsonPath("$.data.category").value("FOOD"))
                .andExpect(jsonPath("$.data.expenseSplitType").value("EQUAL"))
                .andExpect(jsonPath("$.data.splits.length()").value(2))
                .andExpect(jsonPath("$.message").doesNotExist())
                .andExpect(jsonPath("$.errors").doesNotExist());

        verify(expenseApiMapper)
                .toCommand(
                        eq(idempotencyKey),
                        eq(groupId),
                        eq(request)
                );

        verify(expenseService)
                .createExpense(eq(command));
    }

    @Test
    void createExpense_shouldReturn200Ok_whenIdempotencyKeyIsReplayed() throws Exception {

        UUID groupId = UUID.randomUUID();
        UUID paidBy = UUID.randomUUID();
        UUID participant2 = UUID.randomUUID();

        String idempotencyKey = "expense-replay-001";

        CreateExpenseRequest request = new CreateExpenseRequest(
                paidBy,
                new BigDecimal("100000"),
                ExpenseCategory.FOOD,
                new SplitRequest(
                        ExpenseSplitType.EQUAL,
                        List.of(
                                new SplitParticipantRequest(
                                        paidBy,
                                        null,
                                        null
                                ),
                                new SplitParticipantRequest(
                                        participant2,
                                        null,
                                        null
                                )
                        )
                )
        );

        CreateExpenseCommand command = new CreateExpenseCommand(
                idempotencyKey,
                groupId,
                paidBy,
                com.tafhdev.split_bill_app.shared.domain.Money.of(
                        new BigDecimal("100000")
                ),
                ExpenseCategory.FOOD,
                new com.tafhdev.split_bill_app.expense.service.dto.SplitCommand(
                        ExpenseSplitType.EQUAL,
                        List.of(
                                new com.tafhdev.split_bill_app.expense.service.dto.SplitParticipantCommand(
                                        paidBy,
                                        null,
                                        null
                                ),
                                new com.tafhdev.split_bill_app.expense.service.dto.SplitParticipantCommand(
                                        participant2,
                                        null,
                                        null
                                )
                        )
                )
        );

        ExpenseResponse response = new ExpenseResponse(
                UUID.randomUUID(),
                groupId,
                paidBy,
                new BigDecimal("100000"),
                ExpenseCategory.FOOD,
                ExpenseSplitType.EQUAL,
                List.of(),
                Instant.parse("2026-10-06T10:00:00Z")
        );

        when(expenseApiMapper.toCommand(
                eq(idempotencyKey),
                eq(groupId),
                eq(request)
        )).thenReturn(command);

        when(expenseService.createExpense(eq(command)))
                .thenReturn(new ExpenseResult(response, true));

        mockMvc.perform(
                        post("/api/groups/{groupId}/expenses", groupId)
                                .header("Idempotency-Key", idempotencyKey)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(response.id().toString()))
                .andExpect(jsonPath("$.data.groupId").value(groupId.toString()));

        verify(expenseApiMapper)
                .toCommand(
                        eq(idempotencyKey),
                        eq(groupId),
                        eq(request)
                );

        verify(expenseService)
                .createExpense(eq(command));
    }

    @Test
    void createExpense_shouldReturn400_whenPaidByIsNull() throws Exception {

        UUID groupId = UUID.randomUUID();
        UUID participantId = UUID.randomUUID();

        CreateExpenseRequest request = new CreateExpenseRequest(
                null,
                new BigDecimal("100000"),
                ExpenseCategory.FOOD,
                new SplitRequest(
                        ExpenseSplitType.EQUAL,
                        List.of(
                                new SplitParticipantRequest(
                                        participantId,
                                        null,
                                        null
                                )
                        )
                )
        );

        mockMvc.perform(
                        post("/api/groups/{groupId}/expenses", groupId)
                                .header("Idempotency-Key", "expense-validation-001")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.errors.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors.details.paidBy")
                        .value("must not be null"));
    }

    @Test
    void createExpense_shouldReturn400_whenAmountIsNull() throws Exception {

        UUID groupId = UUID.randomUUID();
        UUID paidBy = UUID.randomUUID();

        CreateExpenseRequest request = new CreateExpenseRequest(
                paidBy,
                null,
                ExpenseCategory.FOOD,
                new SplitRequest(
                        ExpenseSplitType.EQUAL,
                        List.of(
                                new SplitParticipantRequest(
                                        paidBy,
                                        null,
                                        null
                                )
                        )
                )
        );

        mockMvc.perform(
                        post("/api/groups/{groupId}/expenses", groupId)
                                .header("Idempotency-Key", "expense-validation-002")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors.details.amount")
                        .value("must not be null"));
    }

    @Test
    void createExpense_shouldReturn400_whenAmountIsZero() throws Exception {

        UUID groupId = UUID.randomUUID();
        UUID paidBy = UUID.randomUUID();

        CreateExpenseRequest request = new CreateExpenseRequest(
                paidBy,
                BigDecimal.ZERO,
                ExpenseCategory.FOOD,
                new SplitRequest(
                        ExpenseSplitType.EQUAL,
                        List.of(
                                new SplitParticipantRequest(
                                        paidBy,
                                        null,
                                        null
                                )
                        )
                )
        );

        mockMvc.perform(
                        post("/api/groups/{groupId}/expenses", groupId)
                                .header("Idempotency-Key", "expense-validation-003")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors.details.amount")
                        .value("must be greater than or equal to 0.01"));
    }

    @Test
    void createExpense_shouldReturn400_whenCategoryIsNull() throws Exception {

        UUID groupId = UUID.randomUUID();
        UUID paidBy = UUID.randomUUID();

        CreateExpenseRequest request = new CreateExpenseRequest(
                paidBy,
                new BigDecimal("100000"),
                null,
                new SplitRequest(
                        ExpenseSplitType.EQUAL,
                        List.of(
                                new SplitParticipantRequest(
                                        paidBy,
                                        null,
                                        null
                                )
                        )
                )
        );

        mockMvc.perform(
                        post("/api/groups/{groupId}/expenses", groupId)
                                .header("Idempotency-Key", "expense-validation-004")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors.details.category")
                        .value("must not be null"));
    }

    @Test
    void createExpense_shouldReturn400_whenSplitIsNull() throws Exception {

        UUID groupId = UUID.randomUUID();
        UUID paidBy = UUID.randomUUID();

        CreateExpenseRequest request = new CreateExpenseRequest(
                paidBy,
                new BigDecimal("100000"),
                ExpenseCategory.FOOD,
                null
        );

        mockMvc.perform(
                        post("/api/groups/{groupId}/expenses", groupId)
                                .header("Idempotency-Key", "expense-validation-005")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors.details.split")
                        .value("must not be null"));
    }

    @Test
    void createExpense_shouldReturn400_whenSplitTypeIsNull() throws Exception {

        UUID groupId = UUID.randomUUID();
        UUID paidBy = UUID.randomUUID();

        CreateExpenseRequest request = new CreateExpenseRequest(
                paidBy,
                new BigDecimal("100000"),
                ExpenseCategory.FOOD,
                new SplitRequest(
                        null,
                        List.of(
                                new SplitParticipantRequest(
                                        paidBy,
                                        null,
                                        null
                                )
                        )
                )
        );

        mockMvc.perform(
                        post("/api/groups/{groupId}/expenses", groupId)
                                .header("Idempotency-Key", "expense-validation-006")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors.details['split.type']")
                        .value("must not be null"));
    }

    @Test
    void createExpense_shouldReturn400_whenParticipantsAreEmpty() throws Exception {

        UUID groupId = UUID.randomUUID();
        UUID paidBy = UUID.randomUUID();

        CreateExpenseRequest request = new CreateExpenseRequest(
                paidBy,
                new BigDecimal("100000"),
                ExpenseCategory.FOOD,
                new SplitRequest(
                        ExpenseSplitType.EQUAL,
                        List.of()
                )
        );

        mockMvc.perform(
                        post("/api/groups/{groupId}/expenses", groupId)
                                .header("Idempotency-Key", "expense-validation-007")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors.details['split.participants']")
                        .value("must not be empty"));
    }

    @Test
    void createExpense_shouldReturn400_whenParticipantIdIsNull() throws Exception {

        UUID groupId = UUID.randomUUID();
        UUID paidBy = UUID.randomUUID();

        CreateExpenseRequest request = new CreateExpenseRequest(
                paidBy,
                new BigDecimal("100000"),
                ExpenseCategory.FOOD,
                new SplitRequest(
                        ExpenseSplitType.EQUAL,
                        List.of(
                                new SplitParticipantRequest(
                                        null,
                                        null,
                                        null
                                )
                        )
                )
        );

        mockMvc.perform(
                        post("/api/groups/{groupId}/expenses", groupId)
                                .header("Idempotency-Key", "expense-validation-008")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors.details['split.participants[0].participantId']")
                        .value("must not be null"));
    }

    @Test
    void createExpense_shouldReturn400_whenIdempotencyKeyIsBlank() throws Exception {

        UUID groupId = UUID.randomUUID();
        UUID paidBy = UUID.randomUUID();

        CreateExpenseRequest request = new CreateExpenseRequest(
                paidBy,
                new BigDecimal("100000"),
                ExpenseCategory.FOOD,
                new SplitRequest(
                        ExpenseSplitType.EQUAL,
                        List.of(
                                new SplitParticipantRequest(
                                        paidBy,
                                        null,
                                        null
                                )
                        )
                )
        );

        mockMvc.perform(
                        post("/api/groups/{groupId}/expenses", groupId)
                                .header("Idempotency-Key", "   ")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors.details['createExpense.idempotencyKey']")
                        .value("Idempotency-Key must not be blank"));
    }

    @Test
    void createExpense_shouldReturn400_whenIdempotencyKeyIsMissing() throws Exception {

        UUID groupId = UUID.randomUUID();
        UUID paidBy = UUID.randomUUID();

        CreateExpenseRequest request = new CreateExpenseRequest(
                paidBy,
                new BigDecimal("100000"),
                ExpenseCategory.FOOD,
                new SplitRequest(
                        ExpenseSplitType.EQUAL,
                        List.of(
                                new SplitParticipantRequest(
                                        paidBy,
                                        null,
                                        null
                                )
                        )
                )
        );

        mockMvc.perform(
                        post("/api/groups/{groupId}/expenses", groupId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.errors.message")
                        .value("Missing required header: Idempotency-Key"));
    }
}