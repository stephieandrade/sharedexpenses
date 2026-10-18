package com.sharedexpenses.balances.infrastructure;

import com.sharedexpenses.balances.application.PaymentRepository;
import com.sharedexpenses.expenses.application.GroupPaymentsRemover;

public class GroupPaymentsRemoverAdapter implements GroupPaymentsRemover {

    private final PaymentRepository payments;

    public GroupPaymentsRemoverAdapter(PaymentRepository payments) {
        this.payments = payments;
    }

    @Override
    public void removeAllPayments(Long groupId) {
        payments.deleteByGroupId(groupId);
    }
}
