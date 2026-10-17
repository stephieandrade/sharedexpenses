package com.sharedexpenses.balances.application;

import com.sharedexpenses.balances.domain.Payment;

import java.util.List;

public interface PaymentRepository {

    Payment save(Payment payment);

    List<Payment> findByGroupId(Long groupId);
}
