package com.tafhdev.split_bill_app.expense.controller.dto.request;

import java.util.List;
import java.util.UUID;

public record SplitRequest(
        
        List<UUID> participants,
        List<ExactSplitRequest> exactSplits,
        List<PercentageSplitRequest> percentageSplits
) {
}
