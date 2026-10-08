package com.tafhdev.split_bill_app.audit.integration;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
class AuditLogIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldReturnAuditLogsEndToEnd() throws Exception {

        GroupTestData group = createGroup();

        createExpense(
                group.groupId(),
                group.taufikId(),
                group.taufikId(),
                group.andiId(),
                new BigDecimal("100000.00")
        );

        createPayment(
                group.groupId(),
                group.andiId(),
                group.taufikId(),
                new BigDecimal("50000.00")
        );

        String response = mockMvc.perform(
                        get(
                                "/api/groups/{groupId}/audit",
                                group.groupId()
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.groupId")
                        .value(group.groupId().toString()))
                .andExpect(jsonPath("$.data.auditLogs")
                        .isArray())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode auditLogs = objectMapper
                .readTree(response)
                .at("/data/auditLogs");

        assertThat(auditLogs).hasSize(2);

        JsonNode paymentAudit = auditLogs.get(0);
        JsonNode expenseAudit = auditLogs.get(1);

        assertThat(expenseAudit.get("action").asString())
                .isEqualTo("CREATED");

        assertThat(expenseAudit.get("entityType").asString())
                .isEqualTo("EXPENSE");

        assertThat(expenseAudit.get("entityId").asString())
                .isNotBlank();

        assertThat(expenseAudit.get("createdAt").asString())
                .isNotBlank();

        assertThat(paymentAudit.get("action").asString())
                .isEqualTo("CREATED");

        assertThat(paymentAudit.get("entityType").asString())
                .isEqualTo("PAYMENT");

        assertThat(paymentAudit.get("entityId").asString())
                .isNotBlank();

        assertThat(paymentAudit.get("createdAt").asString())
                .isNotBlank();
    }

    @Test
    void shouldReturnEmptyAuditLogsWhenGroupHasNoAuditLogs() throws Exception {

        GroupTestData group = createGroup();

        String response = mockMvc.perform(
                        get(
                                "/api/groups/{groupId}/audit",
                                group.groupId()
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.groupId")
                        .value(group.groupId().toString()))
                .andExpect(jsonPath("$.data.auditLogs")
                        .isArray())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode auditLogs = objectMapper
                .readTree(response)
                .at("/data/auditLogs");

        assertThat(auditLogs).isEmpty();
    }

    @Test
    void shouldReturnNotFoundWhenGroupDoesNotExist() throws Exception {

        UUID groupId = UUID.randomUUID();

        mockMvc.perform(
                        get(
                                "/api/groups/{groupId}/audit",
                                groupId
                        )
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
                .andExpect(jsonPath("$.data.participants")
                        .isArray())
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

        for (JsonNode participant : participants) {

            UUID participantId = UUID.fromString(
                    participant.get("id").asString()
            );

            String participantName = participant
                    .get("name")
                    .asString();

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

    private void createPayment(
            UUID groupId,
            UUID fromParticipantId,
            UUID toParticipantId,
            BigDecimal amount
    ) throws Exception {

        mockMvc.perform(
                        post(
                                "/api/groups/{groupId}/payments",
                                groupId
                        )
                                .header(
                                        "Idempotency-Key",
                                        "payment-" + UUID.randomUUID()
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                            "fromParticipantId": "%s",
                                            "toParticipantId": "%s",
                                            "amount": %s
                                        }
                                        """.formatted(
                                        fromParticipantId,
                                        toParticipantId,
                                        amount
                                ))
                )
                .andExpect(status().isCreated());
    }

    private record GroupTestData(
            UUID groupId,
            UUID taufikId,
            UUID andiId
    ) {
    }
}