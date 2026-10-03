package com.tafhdev.split_bill_app.audit.service.dto.command;

import java.util.UUID;

public record GetAuditLogCommand(

        UUID groupId
) {
}
