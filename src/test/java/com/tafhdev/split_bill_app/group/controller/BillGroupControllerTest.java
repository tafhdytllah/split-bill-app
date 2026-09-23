package com.tafhdev.split_bill_app.group.controller;

import com.tafhdev.split_bill_app.group.controller.dto.BillGroupResponse;
import com.tafhdev.split_bill_app.group.controller.dto.ParticipantResponse;
import com.tafhdev.split_bill_app.group.controller.mapper.BillGroupResponseMapper;
import com.tafhdev.split_bill_app.group.domain.BillGroup;
import com.tafhdev.split_bill_app.group.domain.Participant;
import com.tafhdev.split_bill_app.group.service.BillGroupService;
import com.tafhdev.split_bill_app.group.service.dto.CreateBillGroupResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(BillGroupController.class)
class BillGroupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BillGroupService billGroupService;

    @MockitoBean
    private BillGroupResponseMapper billGroupResponseMapper;

    @Test
    void shouldCreateGroup() throws Exception {
        UUID groupId = UUID.randomUUID();
        UUID participantId1 = UUID.randomUUID();
        UUID participantId2 = UUID.randomUUID();

        Instant createdAt =
                Instant.parse("2026-01-01T00:00:00Z");

        Participant taufik = Participant.createNew(
                participantId1,
                groupId,
                "Taufik",
                createdAt
        );

        Participant andi = Participant.createNew(
                participantId2,
                groupId,
                "Andi",
                createdAt
        );

        BillGroup group = BillGroup.createNew(
                groupId,
                "Trip Bandung",
                List.of(taufik, andi),
                createdAt
        );

        CreateBillGroupResult result =
                mock(CreateBillGroupResult.class);

        BillGroupResponse response =
                new BillGroupResponse(
                        groupId,
                        "Trip Bandung",
                        List.of(
                                new ParticipantResponse(
                                        participantId1,
                                        "Taufik"
                                ),
                                new ParticipantResponse(
                                        participantId2,
                                        "Andi"
                                )
                        ),
                        createdAt
                );

        when(billGroupService.createGroup(
                eq("Trip Bandung"),
                eq(List.of("Taufik", "Andi"))
        )).thenReturn(result);

        when(result.billGroup())
                .thenReturn(group);

        when(billGroupResponseMapper.toResponse(group))
                .thenReturn(response);

        mockMvc.perform(
                        post("/api/groups")
                                .contentType("application/json")
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
                .andExpect(jsonPath("$.id")
                        .value(groupId.toString()))
                .andExpect(jsonPath("$.name")
                        .value("Trip Bandung"))
                .andExpect(jsonPath("$.participants[0].id")
                        .value(participantId1.toString()))
                .andExpect(jsonPath("$.participants[0].name")
                        .value("Taufik"))
                .andExpect(jsonPath("$.participants[1].id")
                        .value(participantId2.toString()))
                .andExpect(jsonPath("$.participants[1].name")
                        .value("Andi"))
                .andExpect(jsonPath("$.createdAt")
                        .value("2026-01-01T00:00:00Z"));

        verify(billGroupService).createGroup(
                "Trip Bandung",
                List.of("Taufik", "Andi")
        );

        verify(billGroupResponseMapper).toResponse(group);
    }
}