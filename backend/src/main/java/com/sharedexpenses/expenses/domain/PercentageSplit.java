package com.sharedexpenses.expenses.domain;

import com.sharedexpenses.shared.domain.Amounts;
import com.sharedexpenses.shared.domain.Guard;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.IntStream;

public class PercentageSplit implements SplitStrategy {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);
    private static final int MAX_PERCENTAGE_DECIMALS = 4;

    @Override
    public SplitType type() {
        return SplitType.PERCENTAGES;
    }

    @Override
    public List<ExpenseShare> split(BigDecimal amount, List<Participant> participants) {
        BigDecimal total = Amounts.positive(amount);
        List<Participant> sorted = SplitSupport.sortedParticipants(participants);

        for (Participant p : sorted) {
            Guard.required(p.value(), "Falta el porcentaje del usuario " + p.userId());
            Guard.check(p.value().signum() > 0, "Los porcentajes deben ser mayores que 0");
            Guard.check(p.value().stripTrailingZeros().scale() <= MAX_PERCENTAGE_DECIMALS,
                    "Los porcentajes pueden tener como mucho " + MAX_PERCENTAGE_DECIMALS + " decimales");
        }
        BigDecimal percentageSum = sorted.stream().map(Participant::value).reduce(BigDecimal.ZERO, BigDecimal::add);
        Guard.check(percentageSum.compareTo(HUNDRED) == 0,
                "Los porcentajes suman " + percentageSum + " y deben sumar 100");

        List<BigDecimal> base = sorted.stream()
                .map(p -> total.multiply(p.value()).divide(HUNDRED, Amounts.DECIMALS, RoundingMode.DOWN))
                .toList();
        List<ExpenseShare> shares = SplitSupport.withLeftoverCents(total, sorted, base);
        return IntStream.range(0, shares.size())
                .mapToObj(i -> new ExpenseShare(shares.get(i).userId(), shares.get(i).amount(), sorted.get(i).value()))
                .toList();
    }
}
