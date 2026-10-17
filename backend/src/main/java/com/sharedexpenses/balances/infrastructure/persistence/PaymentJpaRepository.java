package com.sharedexpenses.balances.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface PaymentJpaRepository extends JpaRepository<PaymentEntity, Long> {

    List<PaymentEntity> findByGroupIdOrderByDateAscIdAsc(Long groupId);
}
