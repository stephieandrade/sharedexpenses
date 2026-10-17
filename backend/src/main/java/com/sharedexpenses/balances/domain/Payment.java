package com.sharedexpenses.balances.domain;

import com.sharedexpenses.shared.domain.Amounts;
import com.sharedexpenses.shared.domain.Guard;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Payment(Long id, Long groupId, Long fromUserId, Long toUserId, BigDecimal amount, LocalDate date) {

    public Payment {
        Guard.required(groupId, "El grupo del pago es obligatorio");
        Guard.required(fromUserId, "Hay que indicar quién paga");
        Guard.required(toUserId, "Hay que indicar quién recibe el pago");
        Guard.check(!fromUserId.equals(toUserId), "Un miembro no puede pagarse a sí mismo");
        amount = Amounts.positive(amount);
        Guard.required(date, "La fecha del pago es obligatoria");
    }
}
