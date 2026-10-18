package com.sharedexpenses.expenses.infrastructure;

import com.sharedexpenses.expenses.application.CreateExpenseUseCase;
import com.sharedexpenses.expenses.application.DeleteExpenseUseCase;
import com.sharedexpenses.expenses.application.ExpenseRepository;
import com.sharedexpenses.expenses.application.GroupPaymentsRemover;
import com.sharedexpenses.expenses.application.ListExpensesUseCase;
import com.sharedexpenses.expenses.application.UpdateExpenseUseCase;
import com.sharedexpenses.expenses.domain.AmountSplit;
import com.sharedexpenses.expenses.domain.EqualSplit;
import com.sharedexpenses.expenses.domain.PercentageSplit;
import com.sharedexpenses.expenses.domain.SplitStrategyRegistry;
import com.sharedexpenses.groups.application.GroupAccess;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.util.List;

@Configuration
public class ExpensesConfig {

    @Bean
    SplitStrategyRegistry splitStrategyRegistry() {
        return new SplitStrategyRegistry(List.of(new EqualSplit(), new AmountSplit(), new PercentageSplit()));
    }

    @Bean
    CreateExpenseUseCase createExpenseUseCase(GroupAccess access, ExpenseRepository expenses,
                                              SplitStrategyRegistry strategies, Clock clock) {
        return new CreateExpenseUseCase(access, expenses, strategies, clock);
    }

    @Bean
    UpdateExpenseUseCase updateExpenseUseCase(GroupAccess access, ExpenseRepository expenses,
                                              SplitStrategyRegistry strategies) {
        return new UpdateExpenseUseCase(access, expenses, strategies);
    }

    @Bean
    ListExpensesUseCase listExpensesUseCase(GroupAccess access, ExpenseRepository expenses) {
        return new ListExpensesUseCase(access, expenses);
    }

    @Bean
    DeleteExpenseUseCase deleteExpenseUseCase(GroupAccess access, ExpenseRepository expenses,
                                              GroupPaymentsRemover paymentsRemover) {
        return new DeleteExpenseUseCase(access, expenses, paymentsRemover);
    }
}
