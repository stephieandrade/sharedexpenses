package com.sharedexpenses.balances.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "payments", indexes = @Index(name = "idx_payments_group", columnList = "group_id"))
public class PaymentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "from_user_id", nullable = false)
    private Long fromUserId;

    @Column(name = "to_user_id", nullable = false)
    private Long toUserId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "payment_date", nullable = false)
    private LocalDate date;

    protected PaymentEntity() {
    }

    PaymentEntity(Long id, Long groupId, Long fromUserId, Long toUserId, BigDecimal amount, LocalDate date) {
        this.id = id;
        this.groupId = groupId;
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.amount = amount;
        this.date = date;
    }

    Long getId() {
        return id;
    }

    Long getGroupId() {
        return groupId;
    }

    Long getFromUserId() {
        return fromUserId;
    }

    Long getToUserId() {
        return toUserId;
    }

    BigDecimal getAmount() {
        return amount;
    }

    LocalDate getDate() {
        return date;
    }
}
