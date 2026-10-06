package com.tafhdev.split_bill_app.group.integration;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.tafhdev.split_bill_app.group.persistence.entity.BillGroupEntity;
import com.tafhdev.split_bill_app.group.persistence.repository.BillGroupJpaRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
class BillGroupIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BillGroupJpaRepository billGroupJpaRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateGroupEndToEnd() throws Exception {

        MvcResult result = mockMvc.perform(
                        post("/api/groups")
                                .header("Idempotency-Key", "group-create-001")
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
                .andExpect(jsonPath("$.data.id").exists())
                .andExpect(jsonPath("$.data.name")
                        .value("Trip Bandung"))
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

        BillGroupEntity group = billGroupJpaRepository
                .findByIdWithParticipants(groupId)
                .orElseThrow();

        assertThat(group.getName())
                .isEqualTo("Trip Bandung");

        assertThat(group.getParticipants())
                .hasSize(2);

        assertThat(group.getParticipants())
                .extracting("name")
                .containsExactlyInAnyOrder(
                        "Taufik",
                        "Andi"
                );
    }

    @Test
    void shouldReplayGroupWhenUsingSameIdempotencyKey() throws Exception {

        String request = """
                {
                  "name": "Trip Bandung",
                  "participants": [
                    "Taufik",
                    "Andi"
                  ]
                }
                """;

        MvcResult firstResult = mockMvc.perform(
                        post("/api/groups")
                                .header("Idempotency-Key", "group-replay-001")
                                .contentType(APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode firstResponse = objectMapper.readTree(
                firstResult.getResponse().getContentAsString()
        );

        UUID firstGroupId = UUID.fromString(
                firstResponse.get("data")
                        .get("id")
                        .asString()
        );

        MvcResult secondResult = mockMvc.perform(
                        post("/api/groups")
                                .header("Idempotency-Key", "group-replay-001")
                                .contentType(APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id")
                        .value(firstGroupId.toString()))
                .andExpect(jsonPath("$.data.name")
                        .value("Trip Bandung"))
                .andReturn();

        JsonNode secondResponse = objectMapper.readTree(
                secondResult.getResponse().getContentAsString()
        );

        UUID secondGroupId = UUID.fromString(
                secondResponse.get("data")
                        .get("id")
                        .asString()
        );

        assertThat(secondGroupId)
                .isEqualTo(firstGroupId);

        assertThat(
                billGroupJpaRepository.findByIdWithParticipants(firstGroupId)
        ).isPresent();
    }

    @Test
    void shouldRejectWhenIdempotencyKeyIsReusedWithDifferentRequest() throws Exception {

        String idempotencyKey = "group-conflict-001";

        mockMvc.perform(
                        post("/api/groups")
                                .header("Idempotency-Key", idempotencyKey)
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
                .andExpect(status().isCreated());

        mockMvc.perform(
                        post("/api/groups")
                                .header("Idempotency-Key", idempotencyKey)
                                .contentType(APPLICATION_JSON)
                                .content("""
                                        {
                                          "name": "Trip Jakarta",
                                          "participants": [
                                            "Taufik",
                                            "Budi"
                                          ]
                                        }
                                        """)
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errors.code")
                        .value("CONFLICT"))
                .andExpect(jsonPath("$.errors.message")
                        .value("idempotency key reused with different request"));
    }
}