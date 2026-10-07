package com.tafhdev.split_bill_app.expense.persistence.repository;

import com.tafhdev.split_bill_app.expense.domain.Expense;
import com.tafhdev.split_bill_app.expense.domain.ExpenseCategory;
import com.tafhdev.split_bill_app.expense.domain.ExpenseSplit;
import com.tafhdev.split_bill_app.expense.domain.ExpenseSplitType;
import com.tafhdev.split_bill_app.expense.persistence.entity.ExpenseEntity;
import com.tafhdev.split_bill_app.expense.persistence.entity.ExpenseSplitEntity;
import com.tafhdev.split_bill_app.expense.persistence.mapper.ExpenseMapper;
import com.tafhdev.split_bill_app.expense.persistence.mapper.ExpenseSplitMapper;
import com.tafhdev.split_bill_app.expense.persistence.projection.ExpenseProjection;
import com.tafhdev.split_bill_app.expense.repository.ExpenseRepository;
import com.tafhdev.split_bill_app.group.persistence.entity.BillGroupEntity;
import com.tafhdev.split_bill_app.group.persistence.entity.ParticipantEntity;
import com.tafhdev.split_bill_app.group.persistence.repository.BillGroupJpaRepository;
import com.tafhdev.split_bill_app.group.persistence.repository.ParticipantJpaRepository;
import com.tafhdev.split_bill_app.shared.application.exception.ResourceNotFoundException;
import com.tafhdev.split_bill_app.shared.domain.Money;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
public class ExpenseRepositoryImpl implements ExpenseRepository {

    private final ExpenseJpaRepository expenseJpaRepository;
    private final BillGroupJpaRepository billGroupJpaRepository;
    private final ParticipantJpaRepository participantJpaRepository;
    private final ExpenseMapper expenseMapper;
    private final ExpenseSplitMapper expenseSplitMapper;

    public ExpenseRepositoryImpl(
            ExpenseJpaRepository expenseJpaRepository,
            BillGroupJpaRepository billGroupJpaRepository,
            ParticipantJpaRepository participantJpaRepository,
            ExpenseMapper expenseMapper,
            ExpenseSplitMapper expenseSplitMapper
    ) {
        this.expenseJpaRepository = expenseJpaRepository;
        this.billGroupJpaRepository = billGroupJpaRepository;
        this.participantJpaRepository = participantJpaRepository;
        this.expenseMapper = expenseMapper;
        this.expenseSplitMapper = expenseSplitMapper;
    }

    @Override
    public Expense save(Expense expense) {

        BillGroupEntity groupEntity =
                billGroupJpaRepository.findById(expense.getGroupId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "group not found"
                                ));

        ParticipantEntity paidByEntity =
                participantJpaRepository
                        .findByIdAndGroupId(
                                expense.getPaidBy(),
                                expense.getGroupId()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "paid by participant not found"
                                ));

        List<UUID> participantIds = expense.getSplits().stream()
                .map(ExpenseSplit::getParticipantId)
                .toList();

        List<ParticipantEntity> participantEntities =
                participantJpaRepository.findAllByIdInAndGroupId(
                        participantIds,
                        expense.getGroupId()
                );

        Map<UUID, ParticipantEntity> participantMap =
                participantEntities.stream()
                        .collect(Collectors.toMap(
                                ParticipantEntity::getId,
                                Function.identity()
                        ));

        ExpenseEntity expenseEntity =
                expenseMapper.toEntity(
                        expense,
                        groupEntity,
                        paidByEntity
                );

        for (ExpenseSplit split : expense.getSplits()) {

            ParticipantEntity participantEntity =
                    participantMap.get(split.getParticipantId());

            if (participantEntity == null) {
                throw new ResourceNotFoundException(
                        "split participant not found"
                );
            }

            ExpenseSplitEntity splitEntity =
                    expenseSplitMapper.toEntity(
                            split,
                            expenseEntity,
                            participantEntity
                    );

            expenseEntity.addSplit(splitEntity);
        }

        expenseJpaRepository.save(expenseEntity);

        return expense;
    }

    @Override
    public Optional<Expense> findById(UUID id) {
        return expenseJpaRepository.findByIdWithSplits(id)
                .map(expenseMapper::toDomain);
    }

    @Override
    public List<Expense> findByGroupId(UUID groupId) {
        return expenseJpaRepository.findByGroup_id(groupId).stream()
                .map(expenseMapper::toDomain)
                .toList();
    }

    @Override
    public List<Expense> findByGroupIdNative(UUID groupId) {

        List<ExpenseProjection> rows = expenseJpaRepository.findProjectionByGroupId(groupId);

        if (rows.isEmpty()) {
            return List.of();
        }

        List<Expense> expenses = new ArrayList<>();
        UUID currentExpenseId = null;
        ExpenseProjection firstRow = null;
        List<ExpenseSplit> splits = new ArrayList<>();

        for (ExpenseProjection row : rows) {

            if (!row.expenseId().equals(currentExpenseId)) {

                if (firstRow != null) {
                    expenses.add(expenseMapper.toDomain(firstRow, splits));
                }

                currentExpenseId = row.expenseId();
                firstRow = row;
                splits = new ArrayList<>();
            }

            if (row.splitId() != null) {
                splits.add(
                        ExpenseSplit.reconstitute(
                                row.splitId(),
                                row.participantId(),
                                Money.of(row.splitAmount())
                        )
                );
            }
        }

        if (firstRow != null) {
            expenses.add(expenseMapper.toDomain(firstRow, splits));
        }

        return expenses;
    }
}
