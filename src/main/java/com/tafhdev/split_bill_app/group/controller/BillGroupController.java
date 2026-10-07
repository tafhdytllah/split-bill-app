package com.tafhdev.split_bill_app.group.controller;

import com.tafhdev.split_bill_app.group.controller.dto.BillGroupResponse;
import com.tafhdev.split_bill_app.group.controller.dto.CreateBillGroupRequest;
import com.tafhdev.split_bill_app.group.controller.mapper.BillGroupApiMapper;
import com.tafhdev.split_bill_app.group.service.BillGroupService;
import com.tafhdev.split_bill_app.group.service.dto.BillGroupResult;
import com.tafhdev.split_bill_app.group.service.dto.CreateBillGroupCommand;
import com.tafhdev.split_bill_app.shared.infrastructure.web.response.ApiResponse;
import com.tafhdev.split_bill_app.shared.infrastructure.web.response.ResponseFactory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
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
    public ResponseEntity<ApiResponse<BillGroupResponse>> createGroup(
            @RequestHeader("Idempotency-Key")
            @NotBlank(message = "Idempotency-Key must not be blank")
            String idempotencyKey,

            @Valid
            @RequestBody
            CreateBillGroupRequest request
    ) {

        CreateBillGroupCommand command = billGroupApiMapper.toCommand(
                idempotencyKey,
                request
        );

        BillGroupResult result = billGroupService.createGroup(command);

        return result.replay()
                ? ResponseFactory.ok(result.response())
                : ResponseFactory.created(result.response());
    }
}
