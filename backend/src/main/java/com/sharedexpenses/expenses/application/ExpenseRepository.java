package com.sharedexpenses.expenses.application;

import com.sharedexpenses.expenses.domain.Expense;

import java.util.List;
import java.util.Optional;

public interface ExpenseRepository {

    Expense save(Expense expense);

    Optional<Expense> findById(Long id);

    List<Expense> findByGroupId(Long groupId);

    void deleteById(Long id);
}
