package com.tafhdev.split_bill_app.expense.integration;

import com.tafhdev.split_bill_app.expense.domain.Expense;
import com.tafhdev.split_bill_app.expense.domain.ExpenseCategory;
import com.tafhdev.split_bill_app.expense.domain.ExpenseSplitType;
import com.tafhdev.split_bill_app.expense.repository.ExpenseRepository;
import com.tafhdev.split_bill_app.group.controller.dto.BillGroupResponse;
import com.tafhdev.split_bill_app.group.service.BillGroupService;
import com.tafhdev.split_bill_app.group.service.dto.BillGroupResult;
import com.tafhdev.split_bill_app.group.service.dto.CreateBillGroupCommand;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
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
    void shouldCreateExpenseEndToEnd() throws Exception {

        // given
        BillGroupResponse group = createGroup(
        );

        UUID groupId = group.id();

        UUID payerId = group.participants()
                .get(0)
                .id();

        UUID secondParticipantId = group.participants()
                .get(1)
                .id();

        String idempotencyKey = UUID.randomUUID().toString();

        String request = """
                {
                    "paidBy": "%s",
                    "amount": 300000.00,
                    "category": "FOOD",
                    "split": {
                        "type": "EQUAL",
                        "participants": [
                            {
                                "participantId": "%s"
                            },
                            {
                                "participantId": "%s"
                            }
                        ]
                    }
                }
                """.formatted(
                payerId,
                payerId,
                secondParticipantId
        );

        // when
        String responseBody = mockMvc.perform(
                        post("/api/groups/{groupId}/expenses", groupId)
                                .header("Idempotency-Key", idempotencyKey)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.groupId")
                        .value(groupId.toString()))
                .andExpect(jsonPath("$.data.paidBy")
                        .value(payerId.toString()))
                .andExpect(jsonPath("$.data.amount")
                        .value("300000.00"))
                .andExpect(jsonPath("$.data.category")
                        .value("FOOD"))
                .andExpect(jsonPath("$.data.expenseSplitType")
                        .value("EQUAL"))
                .andExpect(jsonPath("$.data.splits")
                        .isArray())
                .andExpect(jsonPath("$.data.splits.length()")
                        .value(2))
                .andExpect(jsonPath("$.data.createdAt")
                        .exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // then
        JsonNode root = objectMapper.readTree(responseBody);

        UUID expenseId = UUID.fromString(
                root.get("data")
                        .get("id").asString()
        );

        Expense savedExpense = expenseRepository.findById(expenseId)
                .orElseThrow();

        assertThat(savedExpense.getId())
                .isEqualTo(expenseId);

        assertThat(savedExpense.getGroupId())
                .isEqualTo(groupId);

        assertThat(savedExpense.getPaidBy())
                .isEqualTo(payerId);

        assertThat(savedExpense.getAmount().value())
                .isEqualByComparingTo("300000.00");

        assertThat(savedExpense.getCategory())
                .isEqualTo(ExpenseCategory.FOOD);

        assertThat(savedExpense.getSplitType())
                .isEqualTo(ExpenseSplitType.EQUAL);

        assertThat(savedExpense.getSplits())
                .hasSize(2);
    }

    @Test
    void shouldReplayExpenseWhenUsingSameIdempotencyKey() throws Exception {

        // given
        BillGroupResponse group = createGroup(
        );

        UUID groupId = group.id();

        UUID payerId = group.participants()
                .get(0)
                .id();

        UUID secondParticipantId = group.participants()
                .get(1)
                .id();

        String idempotencyKey = UUID.randomUUID().toString();

        String request = """
                {
                    "paidBy": "%s",
                    "amount": 300000.00,
                    "category": "FOOD",
                    "split": {
                        "type": "EQUAL",
                        "participants": [
                            {
                                "participantId": "%s"
                            },
                            {
                                "participantId": "%s"
                            }
                        ]
                    }
                }
                """.formatted(
                payerId,
                payerId,
                secondParticipantId
        );

        // first request
        String firstResponseBody = mockMvc.perform(
                        post("/api/groups/{groupId}/expenses", groupId)
                                .header(
                                        "Idempotency-Key",
                                        idempotencyKey
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID firstExpenseId = UUID.fromString(
                objectMapper.readTree(firstResponseBody)
                        .get("data")
                        .get("id").asString()
        );

        // second request
        String secondResponseBody = mockMvc.perform(
                        post("/api/groups/{groupId}/expenses", groupId)
                                .header(
                                        "Idempotency-Key",
                                        idempotencyKey
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id")
                        .value(firstExpenseId.toString()))
                .andExpect(jsonPath("$.data.groupId")
                        .value(groupId.toString()))
                .andExpect(jsonPath("$.data.paidBy")
                        .value(payerId.toString()))
                .andExpect(jsonPath("$.data.amount")
                        .value("300000.00"))
                .andExpect(jsonPath("$.data.category")
                        .value("FOOD"))
                .andExpect(jsonPath("$.data.expenseSplitType")
                        .value("EQUAL"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // then
        UUID secondExpenseId = UUID.fromString(
                objectMapper.readTree(secondResponseBody)
                        .get("data")
                        .get("id").asString()
        );

        assertThat(secondExpenseId)
                .isEqualTo(firstExpenseId);

        assertThat(expenseRepository.findById(firstExpenseId))
                .isPresent();
    }

    @Test
    void shouldReturnNotFoundWhenGroupDoesNotExist() throws Exception {

        // given
        UUID groupId = UUID.randomUUID();
        UUID participantId = UUID.randomUUID();

        String request = """
                {
                    "paidBy": "%s",
                    "amount": 300000.00,
                    "category": "FOOD",
                    "split": {
                        "type": "EQUAL",
                        "participants": [
                            {
                                "participantId": "%s"
                            }
                        ]
                    }
                }
                """.formatted(
                participantId,
                participantId
        );

        // when & then
        mockMvc.perform(
                        post("/api/groups/{groupId}/expenses", groupId)
                                .header(
                                        "Idempotency-Key",
                                        UUID.randomUUID().toString()
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors.code")
                        .value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.errors.message")
                        .value("bill group not found"));
    }

    @Test
    void shouldRejectExpenseWhenPayerIsNotInGroup() throws Exception {

        // given
        BillGroupResponse group = createGroup(
        );

        UUID groupId = group.id();

        UUID payerId = UUID.randomUUID();

        UUID participantId = group.participants()
                .getFirst()
                .id();

        String request = """
                {
                    "paidBy": "%s",
                    "amount": 300000.00,
                    "category": "FOOD",
                    "split": {
                        "type": "EQUAL",
                        "participants": [
                            {
                                "participantId": "%s"
                            }
                        ]
                    }
                }
                """.formatted(
                payerId,
                participantId
        );

        // when & then
        mockMvc.perform(
                        post("/api/groups/{groupId}/expenses", groupId)
                                .header(
                                        "Idempotency-Key",
                                        UUID.randomUUID().toString()
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.code")
                        .value("DOMAIN_ERROR"));
    }

    private BillGroupResponse createGroup() {

        CreateBillGroupCommand command =
                new CreateBillGroupCommand(
                        UUID.randomUUID().toString(),
                        "Trip Bandung",
                        java.util.List.of(new String[]{"Taufik", "Budi"})
                );

        BillGroupResult result =
                billGroupService.createGroup(command);

        return result.response();
    }
}