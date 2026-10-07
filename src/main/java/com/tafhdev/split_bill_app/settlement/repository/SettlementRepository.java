package com.tafhdev.split_bill_app.settlement.repository;

import com.tafhdev.split_bill_app.settlement.domain.Balance;

import java.util.List;
import java.util.UUID;

public interface SettlementRepository {

    // not used
    List<Balance> findBalances(UUID groupId);
}
