package com.tafhdev.split_bill_app.expense.controller;

import com.tafhdev.split_bill_app.expense.controller.dto.request.CreateExpenseRequest;
import com.tafhdev.split_bill_app.expense.controller.dto.request.ExactSplitRequest;
import com.tafhdev.split_bill_app.expense.controller.dto.request.PercentageSplitRequest;
import com.tafhdev.split_bill_app.expense.controller.dto.request.SplitRequest;
import com.tafhdev.split_bill_app.expense.domain.Expense;
import com.tafhdev.split_bill_app.expense.domain.ExpenseCategory;
import com.tafhdev.split_bill_app.expense.domain.ExpenseSplit;
import com.tafhdev.split_bill_app.expense.domain.SplitType;
import com.tafhdev.split_bill_app.expense.service.ExpenseService;
import com.tafhdev.split_bill_app.expense.service.dto.CreateExpenseResult;
import com.tafhdev.split_bill_app.group.domain.Participant;
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
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(ExpenseController.class)
class ExpenseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ExpenseService expenseService;

    @Test
    void shouldCreateExpense() throws Exception {

        UUID groupId = UUID.randomUUID();
        UUID expenseId = UUID.randomUUID();
        UUID paidBy = UUID.randomUUID();
        UUID participant1 = UUID.randomUUID();
        UUID participant2 = UUID.randomUUID();

        Money expenseAmount =
                Money.of(new BigDecimal("300000.00"));

        Instant createdAt =
                Instant.parse("2026-09-16T10:00:00Z");

        Participant participant1Entity = Participant.createNew(
                participant1,
                groupId,
                "Taufik",
                createdAt
        );

        Participant participant2Entity = Participant.createNew(
                participant2,
                groupId,
                "Budi",
                createdAt
        );

        ExpenseSplit split1 = ExpenseSplit.createNew(
                UUID.randomUUID(),
                participant1,
                Money.of(new BigDecimal("150000.00"))
        );

        ExpenseSplit split2 = ExpenseSplit.createNew(
                UUID.randomUUID(),
                participant2,
                Money.of(new BigDecimal("150000.00"))
        );

        Expense expense = Expense.createNew(
                expenseId,
                groupId,
                paidBy,
                expenseAmount,
                ExpenseCategory.FOOD,
                SplitType.EQUAL,
                List.of(split1, split2),
                createdAt
        );

        CreateExpenseResult result =
                new CreateExpenseResult(
                        expense,
                        List.of(
                                participant1Entity,
                                participant2Entity
                        )
                );

        CreateExpenseRequest request =
                new CreateExpenseRequest(
                        paidBy,
                        new BigDecimal("300000.00"),
                        ExpenseCategory.FOOD,
                        SplitType.EQUAL,
                        new SplitRequest(
                                List.of(participant1, participant2),
                                null,
                                null
                        )
                );

        when(expenseService.createExpense(
                eq(groupId),
                eq(request)
        )).thenReturn(result);

        mockMvc.perform(
                        post("/api/groups/{groupId}/expenses", groupId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id")
                        .value(expenseId.toString()))
                .andExpect(jsonPath("$.groupId")
                        .value(groupId.toString()))
                .andExpect(jsonPath("$.paidBy")
                        .value(paidBy.toString()))
                .andExpect(jsonPath("$.amount")
                        .value(300000.00))
                .andExpect(jsonPath("$.category")
                        .value("FOOD"))
                .andExpect(jsonPath("$.splitType")
                        .value("EQUAL"))
                .andExpect(jsonPath("$.splits")
                        .isArray())
                .andExpect(jsonPath("$.splits.length()")
                        .value(2))
                .andExpect(jsonPath("$.splits[0].participantId")
                        .value(participant1.toString()))
                .andExpect(jsonPath("$.splits[0].name")
                        .value("Taufik"))
                .andExpect(jsonPath("$.splits[0].amount")
                        .value(150000.00))
                .andExpect(jsonPath("$.splits[1].participantId")
                        .value(participant2.toString()))
                .andExpect(jsonPath("$.splits[1].name")
                        .value("Budi"))
                .andExpect(jsonPath("$.splits[1].amount")
                        .value(150000.00))
                .andExpect(jsonPath("$.createdAt")
                        .value("2026-09-16T10:00:00Z"));

        verify(expenseService)
                .createExpense(
                        eq(groupId),
                        eq(request)
                );
    }

    @Test
    void shouldCreateExpenseUsingExactSplit() throws Exception {

        UUID groupId = UUID.randomUUID();
        UUID expenseId = UUID.randomUUID();
        UUID paidBy = UUID.randomUUID();
        UUID participant1 = UUID.randomUUID();
        UUID participant2 = UUID.randomUUID();

        Money expenseAmount =
                Money.of(new BigDecimal("300000.00"));

        Instant createdAt =
                Instant.parse("2026-09-16T10:00:00Z");

        Participant participant1Entity = Participant.createNew(
                participant1,
                groupId,
                "Taufik",
                createdAt
        );

        Participant participant2Entity = Participant.createNew(
                participant2,
                groupId,
                "Budi",
                createdAt
        );

        List<ExactSplitRequest> exactSplitRequests = List.of(
                new ExactSplitRequest(
                        participant1,
                        new BigDecimal("200000.00")
                ),
                new ExactSplitRequest(
                        participant2,
                        new BigDecimal("100000.00")
                )
        );

        ExpenseSplit split1 = ExpenseSplit.createNew(
                UUID.randomUUID(),
                participant1,
                Money.of(new BigDecimal("200000.00"))
        );

        ExpenseSplit split2 = ExpenseSplit.createNew(
                UUID.randomUUID(),
                participant2,
                Money.of(new BigDecimal("100000.00"))
        );

        Expense expense = Expense.createNew(
                expenseId,
                groupId,
                paidBy,
                expenseAmount,
                ExpenseCategory.FOOD,
                SplitType.EXACT,
                List.of(split1, split2),
                createdAt
        );

        CreateExpenseResult result =
                new CreateExpenseResult(
                        expense,
                        List.of(
                                participant1Entity,
                                participant2Entity
                        )
                );

        CreateExpenseRequest request =
                new CreateExpenseRequest(
                        paidBy,
                        new BigDecimal("300000.00"),
                        ExpenseCategory.FOOD,
                        SplitType.EXACT,
                        new SplitRequest(
                                null,
                                exactSplitRequests,
                                null
                        )
                );

        when(expenseService.createExpense(
                eq(groupId),
                eq(request)
        )).thenReturn(result);

        mockMvc.perform(
                        post("/api/groups/{groupId}/expenses", groupId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id")
                        .value(expenseId.toString()))
                .andExpect(jsonPath("$.groupId")
                        .value(groupId.toString()))
                .andExpect(jsonPath("$.paidBy")
                        .value(paidBy.toString()))
                .andExpect(jsonPath("$.amount")
                        .value(300000.00))
                .andExpect(jsonPath("$.category")
                        .value("FOOD"))
                .andExpect(jsonPath("$.splitType")
                        .value("EXACT"))
                .andExpect(jsonPath("$.splits.length()")
                        .value(2))
                .andExpect(jsonPath("$.splits[0].participantId")
                        .value(participant1.toString()))
                .andExpect(jsonPath("$.splits[0].name")
                        .value("Taufik"))
                .andExpect(jsonPath("$.splits[0].amount")
                        .value(200000.00))
                .andExpect(jsonPath("$.splits[1].participantId")
                        .value(participant2.toString()))
                .andExpect(jsonPath("$.splits[1].name")
                        .value("Budi"))
                .andExpect(jsonPath("$.splits[1].amount")
                        .value(100000.00))
                .andExpect(jsonPath("$.createdAt")
                        .value("2026-09-16T10:00:00Z"));

        verify(expenseService)
                .createExpense(
                        eq(groupId),
                        eq(request)
                );
    }

    @Test
    void shouldCreateExpenseUsingPercentageSplit() throws Exception {

        UUID groupId = UUID.randomUUID();
        UUID expenseId = UUID.randomUUID();
        UUID paidBy = UUID.randomUUID();
        UUID participant1 = UUID.randomUUID();
        UUID participant2 = UUID.randomUUID();

        Money expenseAmount =
                Money.of(new BigDecimal("300000.00"));

        Instant createdAt =
                Instant.parse("2026-09-16T10:00:00Z");

        Participant participant1Entity = Participant.createNew(
                participant1,
                groupId,
                "Taufik",
                createdAt
        );

        Participant participant2Entity = Participant.createNew(
                participant2,
                groupId,
                "Budi",
                createdAt
        );

        List<PercentageSplitRequest> percentageSplitRequests = List.of(
                new PercentageSplitRequest(
                        participant1,
                        new BigDecimal("60")
                ),
                new PercentageSplitRequest(
                        participant2,
                        new BigDecimal("40")
                )
        );

        ExpenseSplit split1 = ExpenseSplit.createNew(
                UUID.randomUUID(),
                participant1,
                Money.of(new BigDecimal("180000.00"))
        );

        ExpenseSplit split2 = ExpenseSplit.createNew(
                UUID.randomUUID(),
                participant2,
                Money.of(new BigDecimal("120000.00"))
        );

        Expense expense = Expense.createNew(
                expenseId,
                groupId,
                paidBy,
                expenseAmount,
                ExpenseCategory.FOOD,
                SplitType.PERCENTAGE,
                List.of(split1, split2),
                createdAt
        );

        CreateExpenseResult result =
                new CreateExpenseResult(
                        expense,
                        List.of(
                                participant1Entity,
                                participant2Entity
                        )
                );

        CreateExpenseRequest request =
                new CreateExpenseRequest(
                        paidBy,
                        new BigDecimal("300000.00"),
                        ExpenseCategory.FOOD,
                        SplitType.PERCENTAGE,
                        new SplitRequest(
                                null,
                                null,
                                percentageSplitRequests
                        )
                );

        when(expenseService.createExpense(
                eq(groupId),
                eq(request)
        )).thenReturn(result);

        mockMvc.perform(
                        post("/api/groups/{groupId}/expenses", groupId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id")
                        .value(expenseId.toString()))
                .andExpect(jsonPath("$.groupId")
                        .value(groupId.toString()))
                .andExpect(jsonPath("$.paidBy")
                        .value(paidBy.toString()))
                .andExpect(jsonPath("$.amount")
                        .value(300000.00))
                .andExpect(jsonPath("$.category")
                        .value("FOOD"))
                .andExpect(jsonPath("$.splitType")
                        .value("PERCENTAGE"))
                .andExpect(jsonPath("$.splits.length()")
                        .value(2))
                .andExpect(jsonPath("$.splits[0].participantId")
                        .value(participant1.toString()))
                .andExpect(jsonPath("$.splits[0].name")
                        .value("Taufik"))
                .andExpect(jsonPath("$.splits[0].amount")
                        .value(180000.00))
                .andExpect(jsonPath("$.splits[1].participantId")
                        .value(participant2.toString()))
                .andExpect(jsonPath("$.splits[1].name")
                        .value("Budi"))
                .andExpect(jsonPath("$.splits[1].amount")
                        .value(120000.00))
                .andExpect(jsonPath("$.createdAt")
                        .value("2026-09-16T10:00:00Z"));

        verify(expenseService)
                .createExpense(
                        eq(groupId),
                        eq(request)
                );
    }

    @Test
    void shouldRejectNullPaidBy() throws Exception {

        UUID groupId = UUID.randomUUID();

        CreateExpenseRequest request =
                new CreateExpenseRequest(
                        null,
                        new BigDecimal("300000.00"),
                        ExpenseCategory.FOOD,
                        SplitType.EQUAL,
                        new SplitRequest(
                                List.of(
                                        UUID.randomUUID(),
                                        UUID.randomUUID()
                                ),
                                null,
                                null
                        )
                );

        mockMvc.perform(
                        post("/api/groups/{groupId}/expenses", groupId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(expenseService);
    }

    @Test
    void shouldRejectNullAmount() throws Exception {

        UUID groupId = UUID.randomUUID();

        CreateExpenseRequest request =
                new CreateExpenseRequest(
                        UUID.randomUUID(),
                        null,
                        ExpenseCategory.FOOD,
                        SplitType.EQUAL,
                        new SplitRequest(
                                List.of(
                                        UUID.randomUUID(),
                                        UUID.randomUUID()
                                ),
                                null,
                                null
                        )
                );

        mockMvc.perform(
                        post("/api/groups/{groupId}/expenses", groupId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(expenseService);
    }

    @Test
    void shouldRejectZeroAmount() throws Exception {

        UUID groupId = UUID.randomUUID();

        CreateExpenseRequest request =
                new CreateExpenseRequest(
                        UUID.randomUUID(),
                        BigDecimal.ZERO,
                        ExpenseCategory.FOOD,
                        SplitType.EQUAL,
                        new SplitRequest(
                                List.of(
                                        UUID.randomUUID(),
                                        UUID.randomUUID()
                                ),
                                null,
                                null
                        )
                );

        mockMvc.perform(
                        post("/api/groups/{groupId}/expenses", groupId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(expenseService);
    }

    @Test
    void shouldRejectNullCategory() throws Exception {

        UUID groupId = UUID.randomUUID();

        CreateExpenseRequest request =
                new CreateExpenseRequest(
                        UUID.randomUUID(),
                        new BigDecimal("300000.00"),
                        null,
                        SplitType.EQUAL,
                        new SplitRequest(
                                List.of(
                                        UUID.randomUUID(),
                                        UUID.randomUUID()
                                ),
                                null,
                                null
                        )
                );

        mockMvc.perform(
                        post("/api/groups/{groupId}/expenses", groupId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(expenseService);
    }

    @Test
    void shouldRejectNullSplitType() throws Exception {

        UUID groupId = UUID.randomUUID();

        CreateExpenseRequest request =
                new CreateExpenseRequest(
                        UUID.randomUUID(),
                        new BigDecimal("300000.00"),
                        ExpenseCategory.FOOD,
                        null,
                        new SplitRequest(
                                List.of(
                                        UUID.randomUUID(),
                                        UUID.randomUUID()
                                ),
                                null,
                                null
                        )
                );

        mockMvc.perform(
                        post("/api/groups/{groupId}/expenses", groupId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isBadRequest());

        verifyNoInteractions(expenseService);
    }
}