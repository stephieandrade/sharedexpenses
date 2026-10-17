package com.sharedexpenses.balances.infrastructure.web;

import com.sharedexpenses.balances.domain.Payment;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PaymentResponse(Long id, Long fromUserId, Long toUserId, BigDecimal amount, LocalDate date) {

    static PaymentResponse from(Payment payment) {
        return new PaymentResponse(payment.id(), payment.fromUserId(), payment.toUserId(), payment.amount(),
                payment.date());
    }
}
