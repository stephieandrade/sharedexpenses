package com.sharedexpenses.balances.application;

import com.sharedexpenses.balances.domain.Payment;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository {

    Payment save(Payment payment);

    Optional<Payment> findById(Long id);

    List<Payment> findByGroupId(Long groupId);

    void deleteById(Long id);

    void deleteByGroupId(Long groupId);
}
