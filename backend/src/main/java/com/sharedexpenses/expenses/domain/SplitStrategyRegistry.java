package com.sharedexpenses.expenses.domain;

import com.sharedexpenses.shared.domain.Guard;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class SplitStrategyRegistry {

    private final Map<SplitType, SplitStrategy> byType = new EnumMap<>(SplitType.class);

    public SplitStrategyRegistry(List<SplitStrategy> strategies) {
        strategies.forEach(s -> byType.put(s.type(), s));
    }

    public SplitStrategy forType(SplitType type) {
        Guard.required(type, "El tipo de reparto es obligatorio");
        return Guard.required(byType.get(type), "Tipo de reparto no soportado: " + type);
    }
}
