package com.tafhdev.split_bill_app.group;

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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class BillGroupIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BillGroupJpaRepository billGroupJpaRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateGroup() throws Exception {

        MvcResult result = mockMvc.perform(
                        post("/api/groups")
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
                .andExpect(jsonPath("$.name")
                        .value("Trip Bandung"))
                .andExpect(jsonPath("$.participants")
                        .isArray())
                .andExpect(jsonPath("$.participants.length()")
                        .value(2))
                .andReturn();

        JsonNode response =
                objectMapper.readTree(
                        result.getResponse().getContentAsString()
                );

        UUID groupId =
                UUID.fromString(
                        response.get("id").asString()
                );

        BillGroupEntity group =
                billGroupJpaRepository
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
}
