package com.sharedexpenses.expenses.domain;

import com.sharedexpenses.shared.domain.BusinessRuleException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SplitStrategyRegistryTest {

    @Test
    void findsTheStrategyForEachType() {
        SplitStrategyRegistry strategies = new SplitStrategyRegistry(List.of(new EqualSplit(), new AmountSplit(), new PercentageSplit()));

        assertThat(strategies.forType(SplitType.EQUAL)).isInstanceOf(EqualSplit.class);
        assertThat(strategies.forType(SplitType.AMOUNTS)).isInstanceOf(AmountSplit.class);
        assertThat(strategies.forType(SplitType.PERCENTAGES)).isInstanceOf(PercentageSplit.class);
    }

    @Test
    void rejectsUnsupportedOrMissingTypes() {
        SplitStrategyRegistry strategies = new SplitStrategyRegistry(List.of(new EqualSplit()));

        assertThatThrownBy(() -> strategies.forType(SplitType.AMOUNTS)).isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> strategies.forType(null)).isInstanceOf(BusinessRuleException.class);
    }
}
