package com.sharedexpenses.balances.application;

import com.sharedexpenses.balances.domain.Balance;
import com.sharedexpenses.balances.domain.BalanceCalculator;
import com.sharedexpenses.balances.domain.DebtSimplifier;
import com.sharedexpenses.balances.domain.Payment;
import com.sharedexpenses.balances.domain.SuggestedPayment;
import com.sharedexpenses.balances.infrastructure.MemberBalanceAdapter;
import com.sharedexpenses.expenses.application.ExpenseRepository;
import com.sharedexpenses.expenses.application.ListExpensesUseCase;
import com.sharedexpenses.expenses.domain.Expense;
import com.sharedexpenses.expenses.domain.ExpenseShare;
import com.sharedexpenses.expenses.domain.SplitType;
import com.sharedexpenses.groups.application.GroupAccess;
import com.sharedexpenses.groups.application.GroupRepository;
import com.sharedexpenses.groups.domain.Group;
import com.sharedexpenses.shared.domain.BusinessRuleException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BalanceUseCasesTest {

    private static final Instant NOW = Instant.parse("2026-10-17T10:00:00Z");
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 17);

    @Mock
    GroupRepository groups;
    @Mock
    ExpenseRepository expenses;
    @Mock
    PaymentRepository payments;

    GroupAccess access;
    CalculateBalancesUseCase calculateBalances;

    @BeforeEach
    void setUp() {
        Group group = new Group(10L, "Cena", null, "EUR", 1L, NOW, List.of(), 0L);
        group.addMember(1L, NOW);
        group.addMember(2L, NOW);
        group.addMember(3L, NOW);
        when(groups.findById(10L)).thenReturn(Optional.of(group));

        access = new GroupAccess(groups);
        calculateBalances = new CalculateBalancesUseCase(access, new ListExpensesUseCase(access, expenses),
                payments, new BalanceCalculator());
    }

    @Test
    void combinesExpensesAndPaymentsOfTheGroup() {
        givenDinnerPaidBy1AndPayment(new BigDecimal("10.00"));

        List<Balance> balances = calculateBalances.execute(10L, 2L);

        assertThat(balances).containsExactly(
                new Balance(1L, new BigDecimal("10.00")),
                new Balance(2L, new BigDecimal("0.00")),
                new Balance(3L, new BigDecimal("-10.00")));
    }

    @Test
    void suggestsTheSettlementFromTheBalances() {
        givenDinnerPaidBy1AndPayment(new BigDecimal("10.00"));

        List<SuggestedPayment> settlement = new GetSuggestedSettlementUseCase(calculateBalances, new DebtSimplifier())
                .execute(10L, 1L);

        assertThat(settlement).containsExactly(new SuggestedPayment(3L, 1L, new BigDecimal("10.00")));
    }

    @Test
    void memberBalanceAdapterReturnsTheBalanceOfOneMember() {
        givenDinnerPaidBy1AndPayment(new BigDecimal("10.00"));

        assertThat(new MemberBalanceAdapter(calculateBalances).balanceOf(10L, 3L)).isEqualByComparingTo("-10.00");
    }

    @Test
    void registersAPaymentBetweenMembers() {
        when(payments.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        RegisterPaymentUseCase registerPayment = new RegisterPaymentUseCase(access, payments, Clock.fixed(NOW, ZoneOffset.UTC));

        Payment payment = registerPayment.execute(
                new RegisterPaymentUseCase.Command(10L, 3L, 3L, 1L, new BigDecimal("10"), null));

        assertThat(payment.amount()).isEqualByComparingTo("10.00");
        assertThat(payment.date()).isEqualTo(TODAY);
    }

    @Test
    void rejectsPaymentsToSomeoneOutsideTheGroup() {
        RegisterPaymentUseCase registerPayment = new RegisterPaymentUseCase(access, payments, Clock.fixed(NOW, ZoneOffset.UTC));

        assertThatThrownBy(() -> registerPayment.execute(
                new RegisterPaymentUseCase.Command(10L, 1L, 1L, 42L, new BigDecimal("10"), TODAY)))
                .isInstanceOf(BusinessRuleException.class);
        verify(payments, never()).save(any());
    }

    private void givenDinnerPaidBy1AndPayment(BigDecimal paid) {
        Expense dinner = new Expense(1L, 10L, "Cena", new BigDecimal("30.00"), 1L, TODAY, SplitType.EQUAL, List.of(
                new ExpenseShare(1L, new BigDecimal("10.00")),
                new ExpenseShare(2L, new BigDecimal("10.00")),
                new ExpenseShare(3L, new BigDecimal("10.00"))));
        when(expenses.findByGroupId(10L)).thenReturn(List.of(dinner));
        when(payments.findByGroupId(10L)).thenReturn(List.of(new Payment(1L, 10L, 2L, 1L, paid, TODAY)));
    }
}
