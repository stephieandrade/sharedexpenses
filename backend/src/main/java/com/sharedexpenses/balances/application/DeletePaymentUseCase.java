package com.sharedexpenses.balances.application;

import com.sharedexpenses.groups.application.GroupAccess;
import com.sharedexpenses.shared.domain.NotFoundException;

public class DeletePaymentUseCase {

    private final GroupAccess access;
    private final PaymentRepository payments;

    public DeletePaymentUseCase(GroupAccess access, PaymentRepository payments) {
        this.access = access;
        this.payments = payments;
    }

    public void execute(Long groupId, Long paymentId, Long requesterId) {
        access.requireMember(groupId, requesterId);
        payments.findById(paymentId)
                .filter(payment -> payment.groupId().equals(groupId))
                .orElseThrow(() -> new NotFoundException("Pago no encontrado"));
        payments.deleteById(paymentId);
    }
}
