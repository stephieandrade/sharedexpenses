package com.sharedexpenses.balances.application;

import com.sharedexpenses.balances.domain.Payment;
import com.sharedexpenses.groups.application.GroupAccess;

import java.util.List;

public class ListPaymentsUseCase {

    private final GroupAccess access;
    private final PaymentRepository payments;

    public ListPaymentsUseCase(GroupAccess access, PaymentRepository payments) {
        this.access = access;
        this.payments = payments;
    }

    public List<Payment> execute(Long groupId, Long requesterId) {
        access.requireMember(groupId, requesterId);
        return payments.findByGroupId(groupId).reversed();
    }
}
