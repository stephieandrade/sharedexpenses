package com.sharedexpenses.balances.infrastructure;

import com.sharedexpenses.balances.application.CalculateBalancesUseCase;
import com.sharedexpenses.balances.application.DeletePaymentUseCase;
import com.sharedexpenses.balances.application.ListPaymentsUseCase;
import com.sharedexpenses.expenses.application.GroupPaymentsRemover;
import com.sharedexpenses.balances.application.GetSuggestedSettlementUseCase;
import com.sharedexpenses.balances.application.PaymentRepository;
import com.sharedexpenses.balances.application.RegisterPaymentUseCase;
import com.sharedexpenses.balances.domain.BalanceCalculator;
import com.sharedexpenses.balances.domain.DebtSimplifier;
import com.sharedexpenses.expenses.application.ListExpensesUseCase;
import com.sharedexpenses.groups.application.GroupAccess;
import com.sharedexpenses.groups.application.MemberBalanceProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class BalancesConfig {

    @Bean
    CalculateBalancesUseCase calculateBalancesUseCase(GroupAccess access, ListExpensesUseCase listExpenses,
                                                      PaymentRepository payments) {
        return new CalculateBalancesUseCase(access, listExpenses, payments, new BalanceCalculator());
    }

    @Bean
    GetSuggestedSettlementUseCase getSuggestedSettlementUseCase(CalculateBalancesUseCase calculateBalances) {
        return new GetSuggestedSettlementUseCase(calculateBalances, new DebtSimplifier());
    }

    @Bean
    RegisterPaymentUseCase registerPaymentUseCase(GroupAccess access, PaymentRepository payments, Clock clock) {
        return new RegisterPaymentUseCase(access, payments, clock);
    }

    @Bean
    ListPaymentsUseCase listPaymentsUseCase(GroupAccess access, PaymentRepository payments) {
        return new ListPaymentsUseCase(access, payments);
    }

    @Bean
    DeletePaymentUseCase deletePaymentUseCase(GroupAccess access, PaymentRepository payments) {
        return new DeletePaymentUseCase(access, payments);
    }

    @Bean
    MemberBalanceProvider memberBalanceProvider(CalculateBalancesUseCase calculateBalances) {
        return new MemberBalanceAdapter(calculateBalances);
    }

    @Bean
    GroupPaymentsRemover groupPaymentsRemover(PaymentRepository payments) {
        return new GroupPaymentsRemoverAdapter(payments);
    }
}
