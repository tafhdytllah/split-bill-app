package com.tafhdev.split_bill_app.expense;

import com.tafhdev.split_bill_app.expense.controller.dto.response.ExpenseResponse;
import com.tafhdev.split_bill_app.expense.domain.Expense;
import tools.jackson.databind.ObjectMapper;
import com.tafhdev.split_bill_app.expense.controller.dto.request.CreateExpenseRequest;
import com.tafhdev.split_bill_app.expense.domain.ExpenseCategory;
import com.tafhdev.split_bill_app.expense.domain.SplitType;
import com.tafhdev.split_bill_app.expense.repository.ExpenseRepository;
import com.tafhdev.split_bill_app.group.domain.BillGroup;
import com.tafhdev.split_bill_app.group.service.BillGroupService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc(addFilters = false)
@SpringBootTest
@Transactional
class ExpenseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BillGroupService billGroupService;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Test
    void shouldCreateExpenseUsingEqualSplit() throws Exception {

        // Create group + participants
        BillGroup group = billGroupService.createGroup(
                "Dinner",
                List.of("Taufik", "Fitri", "Budi")
        );

        UUID groupId = group.getId();

        UUID participant1 =
                group.getParticipants().get(0).getId();

        UUID participant2 =
                group.getParticipants().get(1).getId();

        UUID participant3 =
                group.getParticipants().get(2).getId();

        UUID paidBy = participant1;

        CreateExpenseRequest request =
                new CreateExpenseRequest(
                        paidBy,
                        new BigDecimal("300000.00"),
                        ExpenseCategory.FOOD,
                        SplitType.EQUAL,
                        List.of(
                                participant1,
                                participant2,
                                participant3
                        ),
                        null,
                        null
                );

        // Create expense
        MvcResult result = mockMvc.perform(
                        post(
                                "/api/groups/{groupId}/expenses",
                                groupId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id")
                        .exists())
                .andExpect(jsonPath("$.group_id")
                        .value(groupId.toString()))
                .andExpect(jsonPath("$.paid_by")
                        .value(paidBy.toString()))
                .andExpect(jsonPath("$.amount")
                        .value(300000.00))
                .andExpect(jsonPath("$.category")
                        .value("FOOD"))
                .andExpect(jsonPath("$.split_type")
                        .value("EQUAL"))
                .andExpect(jsonPath("$.splits")
                        .isArray())
                .andExpect(jsonPath("$.splits.length()")
                        .value(3))
                .andExpect(jsonPath("$.created_at")
                        .exists())
                .andReturn();

        // Get generated expense ID from response
        String response =
                result.getResponse().getContentAsString();

        ExpenseResponse expenseResponse =
                objectMapper.readValue(
                        response,
                        ExpenseResponse.class
                );

        UUID expenseId = expenseResponse.id();

        // Verify expense exists in database
        Optional<Expense> savedExpense =
                expenseRepository.findById(expenseId);

        assertThat(savedExpense)
                .isPresent();

        Expense expense = savedExpense.get();

        assertThat(expense.getId())
                .isEqualTo(expenseId);

        assertThat(expense.getGroupId())
                .isEqualTo(groupId);

        assertThat(expense.getPaidBy())
                .isEqualTo(paidBy);

        assertThat(expense.getAmount().value())
                .isEqualByComparingTo(
                        new BigDecimal("300000.00")
                );

        assertThat(expense.getCategory())
                .isEqualTo(ExpenseCategory.FOOD);

        assertThat(expense.getSplitType())
                .isEqualTo(SplitType.EQUAL);

        assertThat(expense.getSplits())
                .hasSize(3);

        assertThat(
                expense.getSplits()
                        .stream()
                        .map(split ->
                                split.getAmount().value())
        )
                .containsExactlyInAnyOrder(
                        new BigDecimal("100000.00"),
                        new BigDecimal("100000.00"),
                        new BigDecimal("100000.00")
                );
    }
}
