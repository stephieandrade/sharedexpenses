package com.sharedexpenses.expenses.infrastructure.persistence;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface ExpenseJpaRepository extends JpaRepository<ExpenseEntity, Long> {

    @EntityGraph(attributePaths = "shares")
    List<ExpenseEntity> findByGroupIdOrderByDateDescIdDesc(Long groupId);
}
