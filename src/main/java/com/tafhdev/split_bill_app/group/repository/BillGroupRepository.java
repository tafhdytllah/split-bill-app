package com.tafhdev.split_bill_app.group.repository;

import com.tafhdev.split_bill_app.group.domain.BillGroup;

import java.util.Optional;
import java.util.UUID;

public interface BillGroupRepository {

    BillGroup save(BillGroup group);

    Optional<BillGroup> findById(UUID groupId);
}
