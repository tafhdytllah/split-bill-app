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
import org.springframework.transaction.annotation.Propagation;

import java.util.UUID;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
                .andReturn();

        JsonNode response = objectMapper.readTree(
                result.getResponse().getContentAsString()
        );

        JsonNode participants = response
                .get("data")
                .get("participants");

        assertThat(participants)
                .hasSize(2);

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

        String idempotencyKey = "group-replay-001";

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
                                .header("Idempotency-Key", idempotencyKey)
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
                                .header("Idempotency-Key", idempotencyKey)
                                .contentType(APPLICATION_JSON)
                                .content(request)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id")
                        .value(firstGroupId.toString()))
                .andExpect(jsonPath("$.data.name")
                        .value("Trip Bandung"))
                .andExpect(jsonPath("$.data.participants")
                        .isArray())
                .andReturn();

        JsonNode secondResponse = objectMapper.readTree(
                secondResult.getResponse().getContentAsString()
        );

        UUID secondGroupId = UUID.fromString(
                secondResponse.get("data")
                        .get("id")
                        .asString()
        );

        JsonNode secondParticipants = secondResponse
                .get("data")
                .get("participants");

        assertThat(secondParticipants)
                .hasSize(2);

        assertThat(secondGroupId)
                .isEqualTo(firstGroupId);

        BillGroupEntity group = billGroupJpaRepository
                .findByIdWithParticipants(firstGroupId)
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
    void shouldRejectWhenIdempotencyKeyIsReusedWithDifferentRequest() throws Exception {

        String idempotencyKey = "group-conflict-001";

        String firstRequest = """
            {
              "name": "Trip Bandung",
              "participants": [
                "Taufik",
                "Andi"
              ]
            }
            """;

        String secondRequest = """
            {
              "name": "Trip Jakarta",
              "participants": [
                "Taufik",
                "Budi"
              ]
            }
            """;

        mockMvc.perform(
                        post("/api/groups")
                                .header("Idempotency-Key", idempotencyKey)
                                .contentType(APPLICATION_JSON)
                                .content(firstRequest)
                )
                .andExpect(status().isCreated());

        mockMvc.perform(
                        post("/api/groups")
                                .header("Idempotency-Key", idempotencyKey)
                                .contentType(APPLICATION_JSON)
                                .content(secondRequest)
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errors.code")
                        .value("CONFLICT"))
                .andExpect(jsonPath("$.errors.message")
                        .value("idempotency key reused with different request"));
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void shouldHandleConcurrentGroupCreationWithSameIdempotencyKey() throws Exception {

        String idempotencyKey = "group-race-" + UUID.randomUUID();

        String request = """
            {
              "name": "Trip Bandung",
              "participants": [
                "Taufik",
                "Andi"
              ]
            }
            """;

        try (ExecutorService executor = Executors.newFixedThreadPool(2)) {

            CountDownLatch start = new CountDownLatch(1);

            Callable<MvcResult> requestA = () -> {

                start.await();

                return mockMvc.perform(
                                post("/api/groups")
                                        .header("Idempotency-Key", idempotencyKey)
                                        .contentType(APPLICATION_JSON)
                                        .content(request)
                        )
                        .andReturn();
            };

            Callable<MvcResult> requestB = () -> {

                start.await();

                return mockMvc.perform(
                                post("/api/groups")
                                        .header("Idempotency-Key", idempotencyKey)
                                        .contentType(APPLICATION_JSON)
                                        .content(request)
                        )
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

            UUID groupIdA = UUID.fromString(
                    responseA.get("data")
                            .get("id")
                            .asString()
            );

            UUID groupIdB = UUID.fromString(
                    responseB.get("data")
                            .get("id")
                            .asString()
            );

            assertThat(groupIdA)
                    .isEqualTo(groupIdB);

            JsonNode participantsA = responseA
                    .get("data")
                    .get("participants");

            JsonNode participantsB = responseB
                    .get("data")
                    .get("participants");

            assertThat(participantsA).hasSize(2);

            assertThat(participantsB).hasSize(2);

            assertThat(billGroupJpaRepository.findByIdWithParticipants(groupIdA))
                    .isPresent();
        }
    }
}