package com.tafhdev.split_bill_app.settlement.persistence.mapper;

import com.tafhdev.split_bill_app.settlement.domain.Balance;
import com.tafhdev.split_bill_app.settlement.persistence.projection.SettlementBalanceProjection;
import com.tafhdev.split_bill_app.shared.domain.Money;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SettlementMapper {

    public Balance toDomain(
            SettlementBalanceProjection projection
    ) {
        return Balance.of(
                projection.participantId(),
                Money.of(projection.balance())
        );
    }

    public List<Balance> toDomain(
            List<SettlementBalanceProjection> projections
    ) {
        return projections.stream()
                .map(this::toDomain)
                .toList();
    }
}