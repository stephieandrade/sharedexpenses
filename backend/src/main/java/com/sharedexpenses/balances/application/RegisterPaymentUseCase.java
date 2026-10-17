package com.sharedexpenses.balances.application;

import com.sharedexpenses.balances.domain.Payment;
import com.sharedexpenses.groups.application.GroupAccess;
import com.sharedexpenses.groups.domain.Group;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

public class RegisterPaymentUseCase {

    public record Command(Long groupId, Long requesterId, Long fromUserId, Long toUserId, BigDecimal amount,
                          LocalDate date) {
    }

    private final GroupAccess access;
    private final PaymentRepository payments;
    private final Clock clock;

    public RegisterPaymentUseCase(GroupAccess access, PaymentRepository payments, Clock clock) {
        this.access = access;
        this.payments = payments;
        this.clock = clock;
    }

    public Payment execute(Command command) {
        Group group = access.requireMember(command.groupId(), command.requesterId());
        LocalDate date = command.date() != null ? command.date() : LocalDate.now(clock);

        Payment payment = new Payment(null, command.groupId(), command.fromUserId(), command.toUserId(),
                command.amount(), date);
        group.requireMembers(List.of(payment.fromUserId(), payment.toUserId()));

        return payments.save(payment);
    }
}
