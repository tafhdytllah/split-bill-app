package com.tafhdev.split_bill_app.group.controller;

import com.tafhdev.split_bill_app.group.controller.dto.BillGroupResponse;
import com.tafhdev.split_bill_app.group.controller.dto.CreateBillGroupRequest;
import com.tafhdev.split_bill_app.group.controller.dto.ParticipantResponse;
import com.tafhdev.split_bill_app.group.domain.BillGroup;
import com.tafhdev.split_bill_app.group.service.BillGroupService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/groups")
public class BillGroupController {

    private final BillGroupService billGroupService;

    public BillGroupController(
            BillGroupService billGroupService
    ) {
        this.billGroupService = billGroupService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BillGroupResponse createGroup(
            @Valid @RequestBody CreateBillGroupRequest request
    ) {

        BillGroup billGroup = billGroupService.createGroup(
                request.name(),
                request.participants()
        );

        return new BillGroupResponse(
                billGroup.getId(),
                billGroup.getName(),
                billGroup.getParticipants().stream()
                        .map(participant ->
                                new ParticipantResponse(
                                        participant.getId(),
                                        participant.getName()
                                )
                        )
                        .toList(),
                billGroup.getCreatedAt()
        );
    }
}
