package com.tafhdev.split_bill_app.group.controller;

import com.tafhdev.split_bill_app.group.controller.dto.BillGroupResponse;
import com.tafhdev.split_bill_app.group.controller.dto.ParticipantResponse;
import com.tafhdev.split_bill_app.group.controller.mapper.BillGroupApiMapper;
import com.tafhdev.split_bill_app.group.service.BillGroupService;
import com.tafhdev.split_bill_app.group.service.dto.BillGroupResult;
import com.tafhdev.split_bill_app.group.service.dto.CreateBillGroupCommand;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(BillGroupController.class)
class BillGroupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BillGroupService billGroupService;

    @MockitoBean
    private BillGroupApiMapper billGroupApiMapper;

    @Test
    void shouldCreateGroup() throws Exception {

        // given
        UUID groupId = UUID.randomUUID();
        UUID participantId1 = UUID.randomUUID();
        UUID participantId2 = UUID.randomUUID();

        String idempotencyKey = "idem-key-1";

        Instant createdAt =
                Instant.parse("2026-01-01T00:00:00Z");

        CreateBillGroupCommand command =
                new CreateBillGroupCommand(
                        idempotencyKey,
                        "Trip Bandung",
                        List.of("Taufik", "Andi")
                );

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

        BillGroupResult result =
                new BillGroupResult(
                        response,
                        false
                );

        when(billGroupApiMapper.toCommand(
                eq(idempotencyKey),
                any()
        )).thenReturn(command);

        when(billGroupService.createGroup(command))
                .thenReturn(result);

        // when & then
        mockMvc.perform(
                        post("/api/groups")
                                .header(
                                        "Idempotency-Key",
                                        idempotencyKey
                                )
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
                .andExpect(jsonPath("$.data.id")
                        .value(groupId.toString()))
                .andExpect(jsonPath("$.data.name")
                        .value("Trip Bandung"))
                .andExpect(jsonPath("$.data.participants[0].id")
                        .value(participantId1.toString()))
                .andExpect(jsonPath("$.data.participants[0].name")
                        .value("Taufik"))
                .andExpect(jsonPath("$.data.participants[1].id")
                        .value(participantId2.toString()))
                .andExpect(jsonPath("$.data.participants[1].name")
                        .value("Andi"))
                .andExpect(jsonPath("$.data.createdAt")
                        .value("2026-01-01T00:00:00Z"));

        verify(billGroupApiMapper)
                .toCommand(
                        eq(idempotencyKey),
                        any()
                );

        verify(billGroupService)
                .createGroup(command);
    }

    @Test
    void shouldReturnOkWhenRequestIsIdempotentReplay() throws Exception {

        // given
        UUID groupId = UUID.randomUUID();

        String idempotencyKey = "idem-key-1";

        Instant createdAt =
                Instant.parse("2026-01-01T00:00:00Z");

        CreateBillGroupCommand command =
                new CreateBillGroupCommand(
                        idempotencyKey,
                        "Trip Bandung",
                        List.of("Taufik", "Andi")
                );

        BillGroupResponse response =
                new BillGroupResponse(
                        groupId,
                        "Trip Bandung",
                        List.of(
                                new ParticipantResponse(
                                        UUID.randomUUID(),
                                        "Taufik"
                                ),
                                new ParticipantResponse(
                                        UUID.randomUUID(),
                                        "Andi"
                                )
                        ),
                        createdAt
                );

        BillGroupResult result =
                new BillGroupResult(
                        response,
                        true
                );

        when(billGroupApiMapper.toCommand(
                eq(idempotencyKey),
                any()
        )).thenReturn(command);

        when(billGroupService.createGroup(command))
                .thenReturn(result);

        // when & then
        mockMvc.perform(
                        post("/api/groups")
                                .header(
                                        "Idempotency-Key",
                                        idempotencyKey
                                )
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
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id")
                        .value(groupId.toString()))
                .andExpect(jsonPath("$.data.name")
                        .value("Trip Bandung"));

        verify(billGroupApiMapper)
                .toCommand(
                        eq(idempotencyKey),
                        any()
                );

        verify(billGroupService)
                .createGroup(command);
    }

    @Test
    void shouldRejectWhenIdempotencyKeyIsMissing() throws Exception {

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
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.errors.code")
                                .value("BAD_REQUEST")
                )
                .andExpect(
                        jsonPath("$.errors.message")
                                .value(
                                        "Missing required header: Idempotency-Key"
                                )
                );
    }

    @Test
    void shouldRejectWhenIdempotencyKeyIsBlank() throws Exception {

        mockMvc.perform(
                        post("/api/groups")
                                .header("Idempotency-Key", " ")
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
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.errors.code")
                                .value("VALIDATION_ERROR")
                )
                .andExpect(
                        jsonPath("$.errors.details")
                                .exists()
                );
    }

    @Test
    void shouldRejectInvalidRequest() throws Exception {

        mockMvc.perform(
                        post("/api/groups")
                                .header(
                                        "Idempotency-Key",
                                        "idem-key-1"
                                )
                                .contentType("application/json")
                                .content("""
                                        {
                                          "name": "",
                                          "participants": []
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.errors.code")
                                .value("VALIDATION_ERROR")
                )
                .andExpect(
                        jsonPath("$.errors.details")
                                .exists()
                );
    }
}