package com.sharedexpenses.expenses.infrastructure.persistence;

import com.sharedexpenses.expenses.domain.SplitType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "expenses", indexes = @Index(name = "idx_expenses_group", columnList = "group_id"))
public class ExpenseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(nullable = false, length = 200)
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "paid_by_id", nullable = false)
    private Long paidById;

    @Column(name = "expense_date", nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(name = "split_type", nullable = false, length = 20)
    private SplitType splitType;

    @Column(name = "created_by_id")
    private Long createdById;

    @ElementCollection
    @CollectionTable(name = "expense_shares", joinColumns = @JoinColumn(name = "expense_id"))
    private List<ExpenseShareEmbeddable> shares = new ArrayList<>();

    protected ExpenseEntity() {
    }

    ExpenseEntity(Long id, Long groupId, String description, BigDecimal amount, Long paidById, LocalDate date,
                  SplitType splitType, List<ExpenseShareEmbeddable> shares, Long createdById) {
        this.id = id;
        this.groupId = groupId;
        this.description = description;
        this.amount = amount;
        this.paidById = paidById;
        this.date = date;
        this.splitType = splitType;
        this.shares = new ArrayList<>(shares);
        this.createdById = createdById;
    }

    Long getId() {
        return id;
    }

    Long getGroupId() {
        return groupId;
    }

    String getDescription() {
        return description;
    }

    BigDecimal getAmount() {
        return amount;
    }

    Long getPaidById() {
        return paidById;
    }

    LocalDate getDate() {
        return date;
    }

    SplitType getSplitType() {
        return splitType;
    }

    Long getCreatedById() {
        return createdById;
    }

    List<ExpenseShareEmbeddable> getShares() {
        return shares;
    }
}
