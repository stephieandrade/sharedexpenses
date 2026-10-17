package com.sharedexpenses.balances.infrastructure.persistence;

import com.sharedexpenses.balances.domain.Payment;

final class PaymentMapper {

    private PaymentMapper() {
    }

    static PaymentEntity toEntity(Payment payment) {
        return new PaymentEntity(payment.id(), payment.groupId(), payment.fromUserId(), payment.toUserId(),
                payment.amount(), payment.date());
    }

    static Payment toDomain(PaymentEntity entity) {
        return new Payment(entity.getId(), entity.getGroupId(), entity.getFromUserId(), entity.getToUserId(),
                entity.getAmount(), entity.getDate());
    }
}
