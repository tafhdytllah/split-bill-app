package com.tafhdev.split_bill_app.payment.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
class PaymentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreatePaymentEndToEnd() throws Exception {

        GroupTestData group = createGroup();

        createExpense(
                group.groupId(),
                group.taufikId(),
                group.taufikId(),
                group.andiId(),
                new BigDecimal("100000.00")
        );

        String response = mockMvc.perform(
                        post(
                                "/api/groups/{groupId}/payments",
                                group.groupId()
                        )
                                .header(
                                        "Idempotency-Key",
                                        "payment-key-1"
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "fromParticipantId": "%s",
                                            "toParticipantId": "%s",
                                            "amount": 50000.00
                                        }
                                        """.formatted(
                                        group.andiId(),
                                        group.taufikId()
                                ))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.groupId")
                        .value(group.groupId().toString()))
                .andExpect(jsonPath("$.data.fromParticipant.id")
                        .value(group.andiId().toString()))
                .andExpect(jsonPath("$.data.fromParticipant.name")
                        .value("Andi"))
                .andExpect(jsonPath("$.data.toParticipant.id")
                        .value(group.taufikId().toString()))
                .andExpect(jsonPath("$.data.toParticipant.name")
                        .value("Taufik"))
                .andExpect(jsonPath("$.data.amount")
                        .value("50000.00"))
                .andExpect(jsonPath("$.data.createdAt")
                        .exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode data = objectMapper
                .readTree(response)
                .get("data");

        assertThat(data.get("id").asString())
                .isNotBlank();
    }

    @Test
    void shouldReturnOkWhenPaymentIsIdempotentReplay()
            throws Exception {

        GroupTestData group = createGroup();

        createExpense(
                group.groupId(),
                group.taufikId(),
                group.taufikId(),
                group.andiId(),
                new BigDecimal("100000.00")
        );

        String request = """
                {
                    "fromParticipantId": "%s",
                    "toParticipantId": "%s",
                    "amount": 50000.00
                }
                """.formatted(
                group.andiId(),
                group.taufikId()
        );

        String firstResponse = mockMvc.perform(
                        post(
                                "/api/groups/{groupId}/payments",
                                group.groupId()
                        )
                                .header(
                                        "Idempotency-Key",
                                        "payment-replay-key"
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String paymentId = objectMapper
                .readTree(firstResponse)
                .at("/data/id").asString();

        mockMvc.perform(
                        post(
                                "/api/groups/{groupId}/payments",
                                group.groupId()
                        )
                                .header(
                                        "Idempotency-Key",
                                        "payment-replay-key"
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id")
                        .value(paymentId))
                .andExpect(jsonPath("$.data.groupId")
                        .value(group.groupId().toString()))
                .andExpect(jsonPath("$.data.fromParticipant.id")
                        .value(group.andiId().toString()))
                .andExpect(jsonPath("$.data.fromParticipant.name")
                        .value("Andi"))
                .andExpect(jsonPath("$.data.toParticipant.id")
                        .value(group.taufikId().toString()))
                .andExpect(jsonPath("$.data.toParticipant.name")
                        .value("Taufik"))
                .andExpect(jsonPath("$.data.amount")
                        .value("50000.00"));
    }

    @Test
    void shouldReturnConflictWhenIdempotencyKeyIsReusedWithDifferentRequest()
            throws Exception {

        GroupTestData group = createGroup();

        createExpense(
                group.groupId(),
                group.taufikId(),
                group.taufikId(),
                group.andiId(),
                new BigDecimal("100000.00")
        );

        String idempotencyKey = "payment-conflict-key";

        mockMvc.perform(
                        post(
                                "/api/groups/{groupId}/payments",
                                group.groupId()
                        )
                                .header(
                                        "Idempotency-Key",
                                        idempotencyKey
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "fromParticipantId": "%s",
                                            "toParticipantId": "%s",
                                            "amount": 50000.00
                                        }
                                        """.formatted(
                                        group.andiId(),
                                        group.taufikId()
                                ))
                )
                .andExpect(status().isCreated());

        mockMvc.perform(
                        post(
                                "/api/groups/{groupId}/payments",
                                group.groupId()
                        )
                                .header(
                                        "Idempotency-Key",
                                        idempotencyKey
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "fromParticipantId": "%s",
                                            "toParticipantId": "%s",
                                            "amount": 40000.00
                                        }
                                        """.formatted(
                                        group.andiId(),
                                        group.taufikId()
                                ))
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errors.code")
                        .value("CONFLICT"))
                .andExpect(jsonPath("$.errors.message")
                        .value(
                                "idempotency key reused with different request"
                        ));
    }

    @Test
    void shouldRejectWhenPaymentAmountExceedsOutstandingDebt()
            throws Exception {

        GroupTestData group = createGroup();

        createExpense(
                group.groupId(),
                group.taufikId(),
                group.taufikId(),
                group.andiId(),
                new BigDecimal("100000.00")
        );

        mockMvc.perform(
                        post(
                                "/api/groups/{groupId}/payments",
                                group.groupId()
                        )
                                .header(
                                        "Idempotency-Key",
                                        "payment-exceeds-key"
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "fromParticipantId": "%s",
                                            "toParticipantId": "%s",
                                            "amount": 50001.00
                                        }
                                        """.formatted(
                                        group.andiId(),
                                        group.taufikId()
                                ))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.code")
                        .value("DOMAIN_ERROR"))
                .andExpect(jsonPath("$.errors.message")
                        .value(
                                "payment amount exceeds outstanding debt"
                        ));
    }

    @Test
    void shouldReturnNotFoundWhenGroupDoesNotExist()
            throws Exception {

        UUID groupId = UUID.randomUUID();
        UUID fromParticipantId = UUID.randomUUID();
        UUID toParticipantId = UUID.randomUUID();

        mockMvc.perform(
                        post(
                                "/api/groups/{groupId}/payments",
                                groupId
                        )
                                .header(
                                        "Idempotency-Key",
                                        "payment-not-found-key"
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "fromParticipantId": "%s",
                                            "toParticipantId": "%s",
                                            "amount": 50000.00
                                        }
                                        """.formatted(
                                        fromParticipantId,
                                        toParticipantId
                                ))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors.code")
                        .value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.errors.message")
                        .value("group not found"));
    }

    private GroupTestData createGroup() throws Exception {

        String response = mockMvc.perform(
                        post("/api/groups")
                                .header(
                                        "Idempotency-Key",
                                        "group-" + UUID.randomUUID()
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "name": "Trip Bandung",
                                            "participants": [
                                                "Taufik",
                                                "Andi"
                                            ]
                                        }
                                        """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.participants").isArray())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode data = objectMapper
                .readTree(response)
                .get("data");

        JsonNode participants = data.get("participants");
        assertThat(participants).hasSize(2);

        UUID groupId = UUID.fromString(
                data.get("id").asString()
        );

        UUID taufikId = null;
        UUID andiId = null;

        for (JsonNode participant : data.get("participants")) {

            UUID participantId = UUID.fromString(
                    participant.get("id").asString()
            );

            String participantName = participant
                    .get("name").asString();

            if ("Taufik".equals(participantName)) {
                taufikId = participantId;
            }

            if ("Andi".equals(participantName)) {
                andiId = participantId;
            }
        }

        assertThat(taufikId).isNotNull();
        assertThat(andiId).isNotNull();

        return new GroupTestData(
                groupId,
                taufikId,
                andiId
        );
    }

    private void createExpense(
            UUID groupId,
            UUID paidBy,
            UUID participant1,
            UUID participant2,
            BigDecimal amount
    ) throws Exception {

        mockMvc.perform(
                        post(
                                "/api/groups/{groupId}/expenses",
                                groupId
                        )
                                .header(
                                        "Idempotency-Key",
                                        "expense-" + UUID.randomUUID()
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "paidBy": "%s",
                                            "amount": %s,
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
                                        paidBy,
                                        amount,
                                        participant1,
                                        participant2
                                ))
                )
                .andExpect(status().isCreated());
    }

    private record GroupTestData(
            UUID groupId,
            UUID taufikId,
            UUID andiId
    ) {}
}
