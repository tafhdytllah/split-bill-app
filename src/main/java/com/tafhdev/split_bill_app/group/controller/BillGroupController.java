package com.tafhdev.split_bill_app.group.controller;

import com.tafhdev.split_bill_app.group.controller.dto.BillGroupResponse;
import com.tafhdev.split_bill_app.group.controller.dto.CreateBillGroupRequest;
import com.tafhdev.split_bill_app.group.controller.mapper.BillGroupApiMapper;
import com.tafhdev.split_bill_app.group.service.BillGroupService;
import com.tafhdev.split_bill_app.group.service.dto.CreateBillGroupCommand;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/groups")
public class BillGroupController {

    private final BillGroupService billGroupService;
    private final BillGroupApiMapper billGroupApiMapper;

    public BillGroupController(
            BillGroupService billGroupService,
            BillGroupApiMapper billGroupApiMapper
    ) {
        this.billGroupService = billGroupService;
        this.billGroupApiMapper = billGroupApiMapper;
    }

    @PostMapping
    public ResponseEntity<BillGroupResponse> createGroup(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody CreateBillGroupRequest request
    ) {

        CreateBillGroupCommand command = billGroupApiMapper.toCommand(
                idempotencyKey,
                request
        );

        BillGroupResponse response = billGroupService.createGroup(command);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}
