package com.sharedexpenses.expenses.infrastructure.persistence;

import com.sharedexpenses.expenses.application.ExpenseRepository;
import com.sharedexpenses.expenses.domain.Expense;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public class ExpensePersistenceAdapter implements ExpenseRepository {

    private final ExpenseJpaRepository jpa;

    public ExpensePersistenceAdapter(ExpenseJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    @Transactional
    public Expense save(Expense expense) {
        return ExpenseMapper.toDomain(jpa.save(ExpenseMapper.toEntity(expense)));
    }

    @Override
    public Optional<Expense> findById(Long id) {
        return jpa.findById(id).map(ExpenseMapper::toDomain);
    }

    @Override
    public List<Expense> findByGroupId(Long groupId) {
        return jpa.findByGroupIdOrderByDateDescIdDesc(groupId).stream().map(ExpenseMapper::toDomain).toList();
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        jpa.deleteById(id);
    }
}
