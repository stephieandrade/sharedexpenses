package com.sharedexpenses.balances.domain;

import java.math.BigDecimal;

public record SuggestedPayment(Long fromUserId, Long toUserId, BigDecimal amount) {
}
