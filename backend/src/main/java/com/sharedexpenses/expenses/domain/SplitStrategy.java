package com.sharedexpenses.expenses.domain;

import java.math.BigDecimal;
import java.util.List;

public interface SplitStrategy {

    SplitType type();

    List<ExpenseShare> split(BigDecimal amount, List<Participant> participants);
}
