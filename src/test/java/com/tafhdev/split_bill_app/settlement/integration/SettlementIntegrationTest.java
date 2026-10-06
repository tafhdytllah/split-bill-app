package com.tafhdev.split_bill_app.settlement.integration;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
class SettlementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCalculateSettlementEndToEnd() throws Exception {

        GroupTestData group = createGroup();

        createExpense(
                group.groupId(),
                group.taufikId(),
                100000,
                group.taufikId(),
                group.andiId()
        );

        MvcResult result = mockMvc.perform(
                        get(
                                "/api/groups/{groupId}/settlements",
                                group.groupId()
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.groupId")
                        .value(group.groupId().toString()))
                .andExpect(jsonPath("$.data.balances")
                        .isArray())
                .andExpect(jsonPath("$.data.balances.length()")
                        .value(2))
                .andExpect(jsonPath("$.data.settlements")
                        .isArray())
                .andExpect(jsonPath("$.data.settlements.length()")
                        .value(1))
                .andReturn();

        JsonNode response = objectMapper.readTree(
                result.getResponse().getContentAsString()
        );

        JsonNode balances = response
                .get("data")
                .get("balances");

        JsonNode settlements = response
                .get("data")
                .get("settlements");

        JsonNode taufikBalance = findBalance(
                balances,
                group.taufikId()
        );

        JsonNode andiBalance = findBalance(
                balances,
                group.andiId()
        );

        assertThat(taufikBalance.get("amount").decimalValue())
                .isEqualByComparingTo("50000.00");

        assertThat(andiBalance.get("amount").decimalValue())
                .isEqualByComparingTo("-50000.00");

        JsonNode settlement = settlements.get(0);

        assertThat(
                UUID.fromString(
                        settlement.get("fromParticipantId").asString()
                )
        )
                .isEqualTo(group.andiId());

        assertThat(
                UUID.fromString(
                        settlement.get("toParticipantId").asString()
                )
        )
                .isEqualTo(group.taufikId());

        assertThat(
                settlement.get("amount").asString()
        )
                .isEqualTo("50000.00");
    }

    @Test
    void shouldReturnEmptySettlementWhenGroupHasNoExpensesAndPayments() throws Exception {

        GroupTestData group = createGroup();

        mockMvc.perform(
                        get(
                                "/api/groups/{groupId}/settlements",
                                group.groupId()
                        )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.groupId")
                        .value(group.groupId().toString()))
                .andExpect(jsonPath("$.data.balances")
                        .isArray())
                .andExpect(jsonPath("$.data.balances.length()")
                        .value(2))
                .andExpect(jsonPath("$.data.settlements")
                        .isArray())
                .andExpect(jsonPath("$.data.settlements.length()")
                        .value(0));
    }

    @Test
    void shouldReturnNotFoundWhenGroupDoesNotExist() throws Exception {

        UUID groupId = UUID.randomUUID();

        mockMvc.perform(
                        get(
                                "/api/groups/{groupId}/settlements",
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

        MvcResult result = mockMvc.perform(
                        post("/api/groups")
                                .header(
                                        "Idempotency-Key",
                                        UUID.randomUUID().toString()
                                )
                                .contentType(APPLICATION_JSON)
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
                .andExpect(jsonPath("$.data.id")
                        .exists())
                .andExpect(jsonPath("$.data.participants")
                        .isArray())
                .andExpect(jsonPath("$.data.participants.length()")
                        .value(2))
                .andReturn();

        JsonNode response = objectMapper.readTree(
                result.getResponse().getContentAsString()
        );

        UUID groupId = UUID.fromString(
                response.get("data")
                        .get("id")
                        .asString()
        );

        JsonNode participants = response
                .get("data")
                .get("participants");

        UUID taufikId = UUID.fromString(
                participants.get(0)
                        .get("id")
                        .asString()
        );

        UUID andiId = UUID.fromString(
                participants.get(1)
                        .get("id")
                        .asString()
        );

        return new GroupTestData(
                groupId,
                taufikId,
                andiId
        );
    }

    private void createExpense(
            UUID groupId,
            UUID paidBy,
            int amount,
            UUID firstParticipantId,
            UUID secondParticipantId
    ) throws Exception {

        mockMvc.perform(
                        post(
                                "/api/groups/{groupId}/expenses",
                                groupId
                        )
                                .header(
                                        "Idempotency-Key",
                                        UUID.randomUUID().toString()
                                )
                                .contentType(APPLICATION_JSON)
                                .content("""
                                        {
                                          "paidBy": "%s",
                                          "amount": %d,
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
                                        firstParticipantId,
                                        secondParticipantId
                                ))
                )
                .andExpect(status().isCreated());
    }

    private JsonNode findBalance(
            JsonNode balances,
            UUID participantId
    ) {
        for (JsonNode balance : balances) {
            if (participantId.toString()
                    .equals(balance.get("participantId").asString())) {
                return balance;
            }
        }

        throw new AssertionError(
                "Balance not found for participant: " + participantId
        );
    }

    private record GroupTestData(
            UUID groupId,
            UUID taufikId,
            UUID andiId
    ) {
    }
}
