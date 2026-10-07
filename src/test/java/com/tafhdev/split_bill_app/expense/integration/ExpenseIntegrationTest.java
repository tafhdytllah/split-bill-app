package com.tafhdev.split_bill_app.expense.integration;

import com.tafhdev.split_bill_app.expense.domain.Expense;
import com.tafhdev.split_bill_app.expense.domain.ExpenseCategory;
import com.tafhdev.split_bill_app.expense.domain.ExpenseSplitType;
import com.tafhdev.split_bill_app.expense.repository.ExpenseRepository;
import com.tafhdev.split_bill_app.group.controller.dto.BillGroupResponse;
import com.tafhdev.split_bill_app.group.service.BillGroupService;
import com.tafhdev.split_bill_app.group.service.dto.BillGroupResult;
import com.tafhdev.split_bill_app.group.service.dto.CreateBillGroupCommand;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Propagation;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

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

        BillGroupResponse group = createGroup();

        UUID groupId = group.id();

        UUID payerId = group.participants().get(0).id();

        UUID secondParticipantId = group.participants().get(1).id();

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
                """.formatted(payerId, payerId, secondParticipantId
        );

        String responseBody = mockMvc
                .perform(post("/api/groups/{groupId}/expenses", groupId)
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
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

        JsonNode root = objectMapper.readTree(responseBody);

        UUID expenseId = UUID.fromString(
                root.get("data").get("id").asString()
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

        BillGroupResponse group = createGroup();

        UUID groupId = group.id();

        UUID payerId = group.participants().get(0).id();

        UUID secondParticipantId = group.participants().get(1).id();

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
                """.formatted(payerId, payerId, secondParticipantId
        );

        String firstResponseBody = mockMvc
                .perform(post("/api/groups/{groupId}/expenses", groupId)
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID firstExpenseId = UUID.fromString(
                objectMapper.readTree(firstResponseBody).get("data").get("id").asString()
        );

        String secondResponseBody = mockMvc
                .perform(post("/api/groups/{groupId}/expenses", groupId)
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
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

        UUID secondExpenseId = UUID.fromString(
                objectMapper.readTree(secondResponseBody).get("data").get("id").asString()
        );

        assertThat(secondExpenseId)
                .isEqualTo(firstExpenseId);

        assertThat(expenseRepository.findById(firstExpenseId))
                .isPresent();
    }

    @Test
    void shouldRejectWhenIdempotencyKeyIsReusedWithDifferentRequest() throws Exception {

        BillGroupResponse group = createGroup();

        UUID groupId = group.id();

        UUID payerId = group.participants().get(0).id();

        UUID secondParticipantId = group.participants().get(1).id();

        String idempotencyKey = UUID.randomUUID().toString();

        String firstRequest = """
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
                """.formatted(payerId, payerId, secondParticipantId
        );

        String secondRequest = """
                {
                    "paidBy": "%s",
                    "amount": 500000.00,
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
                """.formatted(payerId, payerId, secondParticipantId
        );

        mockMvc.perform(post("/api/groups/{groupId}/expenses", groupId)
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(firstRequest))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/groups/{groupId}/expenses", groupId)
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(secondRequest))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errors.code")
                        .value("CONFLICT"))
                .andExpect(jsonPath("$.errors.message")
                        .value(
                                "idempotency key reused with different request"
                        ));
    }

    @Test
    void shouldReturnNotFoundWhenGroupDoesNotExist() throws Exception {

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
                            },
                            {
                                "participantId": "%s"
                            }
                        ]
                    }
                }
                """.formatted(participantId, participantId, UUID.randomUUID()
        );

        mockMvc.perform(post("/api/groups/{groupId}/expenses", groupId)
                        .header("Idempotency-Key", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors.code")
                        .value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.errors.message")
                        .value("bill group not found"));
    }

    @Test
    void shouldRejectExpenseWhenPayerIsNotInGroup() throws Exception {

        BillGroupResponse group = createGroup();

        UUID groupId = group.id();

        UUID payerId = UUID.randomUUID();

        UUID participantId = group.participants().get(0).id();

        UUID secondParticipantId = group.participants().get(1).id();

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
                """.formatted(payerId, participantId, secondParticipantId
        );

        mockMvc.perform(post("/api/groups/{groupId}/expenses", groupId)
                        .header("Idempotency-Key", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.code")
                        .value("DOMAIN_ERROR"));
    }

    @Test
    void shouldRejectExpenseWhenSplitParticipantIsNotInGroup() throws Exception {

        BillGroupResponse group = createGroup();

        UUID groupId = group.id();

        UUID payerId = group.participants().get(0).id();

        UUID outsiderId = UUID.randomUUID();

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
                """.formatted(payerId, payerId, outsiderId
        );

        mockMvc.perform(post("/api/groups/{groupId}/expenses", groupId)
                        .header("Idempotency-Key", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.code")
                        .value("DOMAIN_ERROR"));
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void shouldHandleConcurrentExpenseCreationWithSameIdempotencyKey() throws Exception {

        BillGroupResponse group = createGroup();

        UUID groupId = group.id();

        UUID payerId = group.participants().get(0).id();

        UUID secondParticipantId = group.participants().get(1).id();

        String idempotencyKey = "expense-race-" + UUID.randomUUID();

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
                """.formatted(payerId, payerId, secondParticipantId
        );

        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {

            CountDownLatch start = new CountDownLatch(1);

            Callable<MvcResult> requestA = () -> {

                start.await();

                return mockMvc
                        .perform(post("/api/groups/{groupId}/expenses", groupId)
                                .header("Idempotency-Key", idempotencyKey)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request))
                        .andReturn();
            };

            Callable<MvcResult> requestB = () -> {

                start.await();

                return mockMvc
                        .perform(post("/api/groups/{groupId}/expenses", groupId)
                                .header("Idempotency-Key", idempotencyKey)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request))
                        .andReturn();
            };

            Future<MvcResult> futureA = executor.submit(requestA);

            Future<MvcResult> futureB = executor.submit(requestB);

            start.countDown();

            MvcResult resultA = futureA.get();

            MvcResult resultB = futureB.get();

            int statusA = resultA.getResponse().getStatus();

            int statusB = resultB.getResponse().getStatus();

            assertThat(List.of(statusA, statusB))
                    .containsExactlyInAnyOrder(201, 200);

            JsonNode responseA = objectMapper.readTree(
                    resultA.getResponse().getContentAsString()
            );

            JsonNode responseB = objectMapper.readTree(
                    resultB.getResponse().getContentAsString()
            );

            UUID expenseIdA = UUID.fromString(responseA.get("data").get("id").asString());

            UUID expenseIdB = UUID.fromString(responseB.get("data").get("id").asString());

            assertThat(expenseIdA).isEqualTo(expenseIdB);

            JsonNode splitsA = responseA.get("data").get("splits");

            JsonNode splitsB = responseB.get("data").get("splits");

            assertThat(splitsA).hasSize(2);

            assertThat(splitsB).hasSize(2);

            assertThat(expenseRepository.findById(expenseIdA))
                    .isPresent();
        }
    }

    private BillGroupResponse createGroup() {

        CreateBillGroupCommand command =
                new CreateBillGroupCommand(
                        UUID.randomUUID().toString(),
                        "Trip Bandung",
                        List.of(
                                "Taufik",
                                "Budi"
                        )
                );

        BillGroupResult result = billGroupService.createGroup(command);

        return result.response();
    }
}