package com.sharedexpenses.expenses.domain;

import com.sharedexpenses.shared.domain.Amounts;
import com.sharedexpenses.shared.domain.Guard;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public record Expense(Long id, Long groupId, String description, BigDecimal amount, Long paidById,
                      LocalDate date, SplitType splitType, List<ExpenseShare> shares) {

    public Expense {
        Guard.required(groupId, "El grupo del gasto es obligatorio");
        description = Guard.requiredText(description, "La descripción del gasto es obligatoria");
        amount = Amounts.positive(amount);
        Guard.required(paidById, "Hay que indicar quién pagó el gasto");
        Guard.required(date, "La fecha del gasto es obligatoria");
        Guard.required(splitType, "El tipo de reparto es obligatorio");
        shares = List.copyOf(Guard.required(shares, "Las partes del gasto son obligatorias"));
        validateShares(amount, shares);
    }

    public static Expense create(Long groupId, String description, BigDecimal amount, Long paidById,
                                 LocalDate date, SplitStrategy strategy, List<Participant> participants) {
        List<ExpenseShare> shares = strategy.split(amount, participants);
        return new Expense(null, groupId, description, amount, paidById, date, strategy.type(), shares);
    }

    public Set<Long> involvedUsers() {
        Set<Long> ids = new HashSet<>(shares.stream().map(ExpenseShare::userId).toList());
        ids.add(paidById);
        return ids;
    }

    private static void validateShares(BigDecimal amount, List<ExpenseShare> shares) {
        Guard.check(!shares.isEmpty(), "El gasto debe tener al menos un participante");
        long distinct = shares.stream().map(ExpenseShare::userId).distinct().count();
        Guard.check(distinct == shares.size(), "Un participante no puede aparecer dos veces");
        BigDecimal sum = Amounts.sum(shares.stream().map(ExpenseShare::amount).toList());
        Guard.check(sum.compareTo(amount) == 0,
                "Las partes suman " + sum + " pero el gasto es de " + amount);
    }
}
