package com.sharedexpenses.shared.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;

public final class Amounts {

    public static final int DECIMALS = 2;
    public static final BigDecimal ZERO = BigDecimal.ZERO.setScale(DECIMALS);
    public static final BigDecimal CENT = new BigDecimal("0.01");

    private Amounts() {
    }

    public static BigDecimal normalize(BigDecimal amount) {
        Guard.required(amount, "El importe es obligatorio");
        try {
            return amount.setScale(DECIMALS, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException e) {
            throw new BusinessRuleException("El importe no puede tener más de 2 decimales: " + amount);
        }
    }

    public static BigDecimal positive(BigDecimal amount) {
        BigDecimal normalized = normalize(amount);
        Guard.check(normalized.signum() > 0, "El importe debe ser mayor que 0");
        return normalized;
    }

    public static BigDecimal sum(Collection<BigDecimal> amounts) {
        return amounts.stream().reduce(ZERO, BigDecimal::add);
    }
}
