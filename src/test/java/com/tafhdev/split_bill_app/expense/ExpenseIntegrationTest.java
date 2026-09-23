package com.tafhdev.split_bill_app.expense;

import com.tafhdev.split_bill_app.expense.controller.dto.request.SplitParticipantRequest;
import com.tafhdev.split_bill_app.expense.controller.dto.request.SplitRequest;
import com.tafhdev.split_bill_app.expense.controller.dto.response.ExpenseResponse;
import com.tafhdev.split_bill_app.expense.domain.Expense;
import com.tafhdev.split_bill_app.expense.domain.ExpenseSplit;
import com.tafhdev.split_bill_app.group.service.dto.CreateBillGroupResult;
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
import static org.hamcrest.Matchers.containsInAnyOrder;
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

        CreateBillGroupResult group = billGroupService.createGroup(
                "Dinner",
                List.of("Taufik", "Fitri", "Budi")
        );

        UUID groupId = group.billGroup().getId();

        UUID participant1 =
                group.billGroup().getParticipants().getFirst().getId();

        UUID participant2 =
                group.billGroup().getParticipants().get(1).getId();

        UUID participant3 =
                group.billGroup().getParticipants().get(2).getId();

        CreateExpenseRequest request =
                new CreateExpenseRequest(
                        participant1,
                        new BigDecimal("300000.00"),
                        ExpenseCategory.FOOD,
                        new SplitRequest(
                                SplitType.EQUAL,
                                List.of(
                                        new SplitParticipantRequest(
                                                participant1,
                                                null,
                                                null
                                        ),
                                        new SplitParticipantRequest(
                                                participant2,
                                                null,
                                                null
                                        ),
                                        new SplitParticipantRequest(
                                                participant3,
                                                null,
                                                null
                                        )
                                )
                        )
                );

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
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.groupId")
                        .value(groupId.toString()))
                .andExpect(jsonPath("$.paidBy")
                        .value(participant1.toString()))
                .andExpect(jsonPath("$.amount")
                        .value(300000.00))
                .andExpect(jsonPath("$.category")
                        .value("FOOD"))
                .andExpect(jsonPath("$.splitType")
                        .value("EQUAL"))
                .andExpect(jsonPath("$.splits")
                        .isArray())
                .andExpect(jsonPath("$.splits.length()")
                        .value(3))
                .andExpect(jsonPath("$.splits[*].name")
                        .value(
                                containsInAnyOrder(
                                        "Taufik",
                                        "Fitri",
                                        "Budi"
                                )
                        ))
                .andExpect(jsonPath("$.createdAt")
                        .exists())
                .andReturn();

        ExpenseResponse response =
                objectMapper.readValue(
                        result.getResponse().getContentAsString(),
                        ExpenseResponse.class
                );

        Optional<Expense> savedExpense =
                expenseRepository.findById(response.id());

        assertThat(savedExpense).isPresent();

        Expense expense = savedExpense.get();

        assertThat(expense.getId())
                .isEqualTo(response.id());

        assertThat(expense.getGroupId())
                .isEqualTo(groupId);

        assertThat(expense.getPaidBy())
                .isEqualTo(participant1);

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
                        .map(ExpenseSplit::getParticipantId)
        )
                .containsExactlyInAnyOrder(
                        participant1,
                        participant2,
                        participant3
                );

        assertThat(
                expense.getSplits()
                        .stream()
                        .map(split ->
                                split.getAmount().value()
                        )
        )
                .containsExactlyInAnyOrder(
                        new BigDecimal("100000.00"),
                        new BigDecimal("100000.00"),
                        new BigDecimal("100000.00")
                );
    }

    @Test
    void shouldCreateExpenseUsingExactSplit() throws Exception {

        CreateBillGroupResult group = billGroupService.createGroup(
                "Dinner",
                List.of("Taufik", "Fitri", "Budi")
        );

        UUID groupId = group.billGroup().getId();

        UUID participant1 =
                group.billGroup().getParticipants().getFirst().getId();

        UUID participant2 =
                group.billGroup().getParticipants().get(1).getId();

        UUID participant3 =
                group.billGroup().getParticipants().get(2).getId();

        CreateExpenseRequest request =
                new CreateExpenseRequest(
                        participant1,
                        new BigDecimal("300.00"),
                        ExpenseCategory.ACCOMMODATION,
                        new SplitRequest(
                                SplitType.EXACT,
                                List.of(
                                        new SplitParticipantRequest(
                                                participant1,
                                                new BigDecimal("210.00"),
                                                null
                                        ),
                                        new SplitParticipantRequest(
                                                participant2,
                                                new BigDecimal("50.00"),
                                                null
                                        ),
                                        new SplitParticipantRequest(
                                                participant3,
                                                new BigDecimal("40.00"),
                                                null
                                        )
                                )
                        )
                );

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
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.groupId")
                        .value(groupId.toString()))
                .andExpect(jsonPath("$.paidBy")
                        .value(participant1.toString()))
                .andExpect(jsonPath("$.amount")
                        .value(300.00))
                .andExpect(jsonPath("$.category")
                        .value("ACCOMMODATION"))
                .andExpect(jsonPath("$.splitType")
                        .value("EXACT"))
                .andExpect(jsonPath("$.splits.length()")
                        .value(3))
                .andExpect(jsonPath("$.splits[*].name")
                        .value(
                                containsInAnyOrder(
                                        "Taufik",
                                        "Fitri",
                                        "Budi"
                                )
                        ))
                .andExpect(jsonPath("$.createdAt")
                        .exists())
                .andReturn();

        ExpenseResponse response =
                objectMapper.readValue(
                        result.getResponse().getContentAsString(),
                        ExpenseResponse.class
                );

        Optional<Expense> savedExpense =
                expenseRepository.findById(response.id());

        assertThat(savedExpense).isPresent();

        Expense expense = savedExpense.get();

        assertThat(expense.getAmount().value())
                .isEqualByComparingTo(
                        new BigDecimal("300.00")
                );

        assertThat(expense.getCategory())
                .isEqualTo(ExpenseCategory.ACCOMMODATION);

        assertThat(expense.getSplitType())
                .isEqualTo(SplitType.EXACT);

        assertThat(
                expense.getSplits()
                        .stream()
                        .map(ExpenseSplit::getParticipantId)
        )
                .containsExactlyInAnyOrder(
                        participant1,
                        participant2,
                        participant3
                );

        assertThat(
                expense.getSplits()
                        .stream()
                        .map(split ->
                                split.getAmount().value()
                        )
        )
                .containsExactlyInAnyOrder(
                        new BigDecimal("210.00"),
                        new BigDecimal("50.00"),
                        new BigDecimal("40.00")
                );
    }

    @Test
    void shouldCreateExpenseUsingPercentageSplit() throws Exception {

        CreateBillGroupResult group = billGroupService.createGroup(
                "Lunch",
                List.of("Taufik", "Budi")
        );

        UUID groupId = group.billGroup().getId();

        UUID participant1 =
                group.billGroup().getParticipants().getFirst().getId();

        UUID participant2 =
                group.billGroup().getParticipants().get(1).getId();

        CreateExpenseRequest request =
                new CreateExpenseRequest(
                        participant1,
                        new BigDecimal("100.00"),
                        ExpenseCategory.FOOD,
                        new SplitRequest(
                                SplitType.PERCENTAGE,
                                List.of(
                                        new SplitParticipantRequest(
                                                participant1,
                                                null,
                                                new BigDecimal("60")
                                        ),
                                        new SplitParticipantRequest(
                                                participant2,
                                                null,
                                                new BigDecimal("40")
                                        )
                                )
                        )
                );

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
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.groupId")
                        .value(groupId.toString()))
                .andExpect(jsonPath("$.paidBy")
                        .value(participant1.toString()))
                .andExpect(jsonPath("$.amount")
                        .value(100.00))
                .andExpect(jsonPath("$.category")
                        .value("FOOD"))
                .andExpect(jsonPath("$.splitType")
                        .value("PERCENTAGE"))
                .andExpect(jsonPath("$.splits.length()")
                        .value(2))
                .andExpect(jsonPath("$.splits[*].name")
                        .value(
                                containsInAnyOrder(
                                        "Taufik",
                                        "Budi"
                                )
                        ))
                .andExpect(jsonPath("$.createdAt")
                        .exists())
                .andReturn();

        ExpenseResponse response =
                objectMapper.readValue(
                        result.getResponse().getContentAsString(),
                        ExpenseResponse.class
                );

        Optional<Expense> savedExpense =
                expenseRepository.findById(response.id());

        assertThat(savedExpense).isPresent();

        Expense expense = savedExpense.get();

        assertThat(expense.getAmount().value())
                .isEqualByComparingTo(
                        new BigDecimal("100.00")
                );

        assertThat(expense.getCategory())
                .isEqualTo(ExpenseCategory.FOOD);

        assertThat(expense.getSplitType())
                .isEqualTo(SplitType.PERCENTAGE);

        assertThat(
                expense.getSplits()
                        .stream()
                        .map(ExpenseSplit::getParticipantId)
        )
                .containsExactlyInAnyOrder(
                        participant1,
                        participant2
                );

        assertThat(
                expense.getSplits()
                        .stream()
                        .map(split ->
                                split.getAmount().value()
                        )
        )
                .containsExactlyInAnyOrder(
                        new BigDecimal("60.00"),
                        new BigDecimal("40.00")
                );
    }

    @Test
    void shouldRejectExpenseWhenPaidByNotInGroup() throws Exception {

        CreateBillGroupResult group = billGroupService.createGroup(
                "Dinner",
                List.of("Taufik", "Fitri")
        );

        UUID groupId = group.billGroup().getId();

        UUID participant1 =
                group.billGroup().getParticipants().getFirst().getId();

        UUID participant2 =
                group.billGroup().getParticipants().get(1).getId();

        UUID outsider = UUID.randomUUID();

        CreateExpenseRequest request =
                new CreateExpenseRequest(
                        outsider,
                        new BigDecimal("100000.00"),
                        ExpenseCategory.FOOD,
                        new SplitRequest(
                                SplitType.EQUAL,
                                List.of(
                                        new SplitParticipantRequest(
                                                participant1,
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

        mockMvc.perform(
                        post(
                                "/api/groups/{groupId}/expenses",
                                groupId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectExpenseWhenSplitParticipantNotInGroup() throws Exception {

        CreateBillGroupResult group = billGroupService.createGroup(
                "Dinner",
                List.of("Taufik", "Fitri")
        );

        UUID groupId = group.billGroup().getId();

        UUID participant1 =
                group.billGroup().getParticipants().getFirst().getId();

        UUID participant2 =
                group.billGroup().getParticipants().get(1).getId();

        UUID outsider = UUID.randomUUID();

        CreateExpenseRequest request =
                new CreateExpenseRequest(
                        participant1,
                        new BigDecimal("100000.00"),
                        ExpenseCategory.FOOD,
                        new SplitRequest(
                                SplitType.EQUAL,
                                List.of(
                                        new SplitParticipantRequest(
                                                participant1,
                                                null,
                                                null
                                        ),
                                        new SplitParticipantRequest(
                                                outsider,
                                                null,
                                                null
                                        )
                                )
                        )
                );

        mockMvc.perform(
                        post(
                                "/api/groups/{groupId}/expenses",
                                groupId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnNotFoundWhenGroupDoesNotExist() throws Exception {

        UUID groupId = UUID.randomUUID();
        UUID participantId = UUID.randomUUID();

        CreateExpenseRequest request =
                new CreateExpenseRequest(
                        participantId,
                        new BigDecimal("100000.00"),
                        ExpenseCategory.FOOD,
                        new SplitRequest(
                                SplitType.EQUAL,
                                List.of(
                                        new SplitParticipantRequest(
                                                participantId,
                                                null,
                                                null
                                        ),
                                        new SplitParticipantRequest(
                                                UUID.randomUUID(),
                                                null,
                                                null
                                        )
                                )
                        )
                );

        mockMvc.perform(
                        post(
                                "/api/groups/{groupId}/expenses",
                                groupId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRejectInvalidExpenseRequest() throws Exception {

        CreateBillGroupResult group = billGroupService.createGroup(
                "Dinner",
                List.of("Taufik", "Fitri")
        );

        UUID groupId = group.billGroup().getId();

        CreateExpenseRequest request =
                new CreateExpenseRequest(
                        null,
                        new BigDecimal("0.00"),
                        null,
                        null
                );

        mockMvc.perform(
                        post(
                                "/api/groups/{groupId}/expenses",
                                groupId
                        )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isBadRequest());
    }
}