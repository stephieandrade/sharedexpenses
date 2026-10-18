package com.sharedexpenses.balances.infrastructure.persistence;

import com.sharedexpenses.balances.application.PaymentRepository;
import com.sharedexpenses.balances.domain.Payment;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public class PaymentPersistenceAdapter implements PaymentRepository {

    private final PaymentJpaRepository jpa;

    public PaymentPersistenceAdapter(PaymentJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    @Transactional
    public Payment save(Payment payment) {
        return PaymentMapper.toDomain(jpa.save(PaymentMapper.toEntity(payment)));
    }

    @Override
    public Optional<Payment> findById(Long id) {
        return jpa.findById(id).map(PaymentMapper::toDomain);
    }

    @Override
    public List<Payment> findByGroupId(Long groupId) {
        return jpa.findByGroupIdOrderByDateAscIdAsc(groupId).stream().map(PaymentMapper::toDomain).toList();
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        jpa.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteByGroupId(Long groupId) {
        jpa.deleteAllByGroupId(groupId);
    }
}
