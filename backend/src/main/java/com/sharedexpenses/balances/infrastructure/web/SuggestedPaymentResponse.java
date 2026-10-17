package com.sharedexpenses.balances.infrastructure.web;

import com.sharedexpenses.balances.domain.SuggestedPayment;

import java.math.BigDecimal;

public record SuggestedPaymentResponse(Long fromUserId, Long toUserId, BigDecimal amount) {

    static SuggestedPaymentResponse from(SuggestedPayment payment) {
        return new SuggestedPaymentResponse(payment.fromUserId(), payment.toUserId(), payment.amount());
    }
}
