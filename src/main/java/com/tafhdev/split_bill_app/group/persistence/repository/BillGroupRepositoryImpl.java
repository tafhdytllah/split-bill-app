package com.tafhdev.split_bill_app.group.persistence.repository;

import com.tafhdev.split_bill_app.group.domain.BillGroup;
import com.tafhdev.split_bill_app.group.persistence.entity.BillGroupEntity;
import com.tafhdev.split_bill_app.group.persistence.mapper.BillGroupMapper;
import com.tafhdev.split_bill_app.group.repository.BillGroupRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class BillGroupRepositoryImpl implements BillGroupRepository {

    private final BillGroupJpaRepository billGroupJpaRepository;
    private final BillGroupMapper billGroupMapper;

    public BillGroupRepositoryImpl(
            BillGroupJpaRepository billGroupJpaRepository,
            BillGroupMapper billGroupMapper
    ) {
        this.billGroupJpaRepository = billGroupJpaRepository;
        this.billGroupMapper = billGroupMapper;
    }

    @Override
    public BillGroup save(BillGroup group) {

        BillGroupEntity groupEntity =
                billGroupMapper.toEntity(group);

        billGroupJpaRepository.save(groupEntity);

        return group;
    }

    @Override
    public Optional<BillGroup> findById(UUID groupId) {
        return billGroupJpaRepository.findByIdWithParticipants(groupId)
                .map(billGroupMapper::toDomain);
    }
}
