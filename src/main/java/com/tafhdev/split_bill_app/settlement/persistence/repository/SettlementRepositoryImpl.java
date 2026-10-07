package com.tafhdev.split_bill_app.settlement.persistence.repository;

import com.tafhdev.split_bill_app.settlement.domain.Balance;
import com.tafhdev.split_bill_app.settlement.persistence.mapper.SettlementMapper;
import com.tafhdev.split_bill_app.settlement.persistence.projection.SettlementBalanceProjection;
import com.tafhdev.split_bill_app.settlement.repository.SettlementRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class SettlementRepositoryImpl implements SettlementRepository {

    private final SettlementJpaRepository settlementJpaRepository;
    private final SettlementMapper settlementMapper;

    public SettlementRepositoryImpl(SettlementJpaRepository settlementJpaRepository, SettlementMapper settlementMapper) {
        this.settlementJpaRepository = settlementJpaRepository;
        this.settlementMapper = settlementMapper;
    }

    @Override
    public List<Balance> findBalances(UUID groupId) {
        List<SettlementBalanceProjection> projections = settlementJpaRepository.findBalances(groupId);

        return settlementMapper.toDomain(projections);
    }
}
