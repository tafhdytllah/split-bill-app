package com.tafhdev.split_bill_app.expense.persistence.repository;

import com.tafhdev.split_bill_app.expense.persistence.entity.ExpenseSplitEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ExpenseSplitJpaRepository extends JpaRepository<ExpenseSplitEntity, UUID> {
}
