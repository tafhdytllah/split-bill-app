package com.tafhdev.split_bill_app.group.controller;

import com.tafhdev.split_bill_app.group.controller.dto.BillGroupResponse;
import com.tafhdev.split_bill_app.group.controller.dto.CreateBillGroupRequest;
import com.tafhdev.split_bill_app.group.controller.mapper.BillGroupResponseMapper;
import com.tafhdev.split_bill_app.group.service.BillGroupService;
import com.tafhdev.split_bill_app.group.service.dto.CreateBillGroupResult;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/groups")
public class BillGroupController {

    private final BillGroupService billGroupService;
    private final BillGroupResponseMapper billGroupResponseMapper;

    public BillGroupController(
            BillGroupService billGroupService,
            BillGroupResponseMapper billGroupResponseMapper
    ) {
        this.billGroupService = billGroupService;
        this.billGroupResponseMapper = billGroupResponseMapper;
    }

    @PostMapping
    public ResponseEntity<BillGroupResponse> createGroup(
            @Valid @RequestBody CreateBillGroupRequest request
    ) {

        CreateBillGroupResult result = billGroupService.createGroup(
                request.name(),
                request.participants()
        );

        BillGroupResponse billGroupResponse = billGroupResponseMapper.toResponse(
                result.billGroup()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(billGroupResponse);
    }
}
