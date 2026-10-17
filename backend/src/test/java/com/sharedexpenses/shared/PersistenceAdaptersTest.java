package com.sharedexpenses.shared;

import com.sharedexpenses.balances.domain.Payment;
import com.sharedexpenses.balances.infrastructure.persistence.PaymentPersistenceAdapter;
import com.sharedexpenses.expenses.domain.EqualSplit;
import com.sharedexpenses.expenses.domain.Expense;
import com.sharedexpenses.expenses.domain.ExpenseShare;
import com.sharedexpenses.expenses.domain.Participant;
import com.sharedexpenses.expenses.infrastructure.persistence.ExpensePersistenceAdapter;
import com.sharedexpenses.groups.domain.Group;
import com.sharedexpenses.groups.domain.Member;
import com.sharedexpenses.groups.infrastructure.persistence.GroupPersistenceAdapter;
import com.sharedexpenses.users.domain.User;
import com.sharedexpenses.users.infrastructure.persistence.UserPersistenceAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.dao.OptimisticLockingFailureException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import({UserPersistenceAdapter.class, GroupPersistenceAdapter.class, ExpensePersistenceAdapter.class,
        PaymentPersistenceAdapter.class})
class PersistenceAdaptersTest {

    private static final Instant NOW = Instant.now().truncatedTo(ChronoUnit.SECONDS);
    private static final LocalDate TODAY = LocalDate.of(2026, 10, 17);

    @Autowired
    UserPersistenceAdapter users;
    @Autowired
    GroupPersistenceAdapter groups;
    @Autowired
    ExpensePersistenceAdapter expenses;
    @Autowired
    PaymentPersistenceAdapter payments;
    @Autowired
    TestEntityManager entityManager;

    Long ana;
    Long bea;
    Long carla;

    @BeforeEach
    void setUp() {
        ana = users.save(new User(null, "Ana", "Ana@Test.com", "hash")).id();
        bea = users.save(new User(null, "Bea", "bea@test.com", "hash")).id();
        carla = users.save(new User(null, "Carla", "carla@test.com", "hash")).id();
    }

    @Test
    void findsUsersByEmailAndIds() {
        assertThat(users.findByEmail("ana@test.com")).get().extracting(User::id).isEqualTo(ana);
        assertThat(users.findAllById(List.of(ana, bea))).extracting(User::name).containsExactlyInAnyOrder("Ana", "Bea");
    }

    @Test
    void savesGroupsWithTheirMembersAndUpdatesMembership() {
        Group group = groups.save(Group.create("Piso", "Gastos del piso", null, ana, NOW));
        group.addMember(bea, NOW.plusSeconds(60));
        groups.save(group);

        Group loaded = groups.findById(group.getId()).orElseThrow();
        assertThat(loaded.getName()).isEqualTo("Piso");
        assertThat(loaded.getMembers()).extracting(Member::userId).containsExactly(ana, bea);
        assertThat(groups.findByMember(bea)).extracting(Group::getId).containsExactly(group.getId());
        assertThat(groups.findByMember(carla)).isEmpty();
    }

    @Test
    void rejectsSavingAGroupThatChangedInTheMeantime() {
        Long groupId = groups.save(Group.create("Piso", null, null, ana, NOW)).getId();
        Group first = groups.findById(groupId).orElseThrow();
        Group second = groups.findById(groupId).orElseThrow();

        first.addMember(bea, NOW);
        groups.save(first);
        entityManager.flush();
        second.addMember(carla, NOW);

        assertThatThrownBy(() -> groups.save(second)).isInstanceOf(OptimisticLockingFailureException.class);
    }

    @Test
    void savesExpensesWithTheirSharesAndDeletesThem() {
        Long groupId = groupWithAllUsers();
        Expense saved = expenses.save(Expense.create(groupId, "Cena", new BigDecimal("10.00"), ana, TODAY,
                new EqualSplit(), List.of(Participant.of(ana), Participant.of(bea), Participant.of(carla))));

        Expense loaded = expenses.findByGroupId(groupId).get(0);
        assertThat(loaded.id()).isEqualTo(saved.id());
        assertThat(loaded.shares()).containsExactlyInAnyOrder(
                new ExpenseShare(ana, new BigDecimal("3.34")),
                new ExpenseShare(bea, new BigDecimal("3.33")),
                new ExpenseShare(carla, new BigDecimal("3.33")));

        expenses.deleteById(saved.id());
        assertThat(expenses.findById(saved.id())).isEmpty();
    }

    @Test
    void savesAndListsPaymentsOfAGroup() {
        Long groupId = groupWithAllUsers();
        Long otherGroupId = groupWithAllUsers();
        payments.save(new Payment(null, groupId, bea, ana, new BigDecimal("3.33"), TODAY));
        payments.save(new Payment(null, otherGroupId, bea, ana, new BigDecimal("1.00"), TODAY));

        assertThat(payments.findByGroupId(groupId)).singleElement()
                .satisfies(p -> assertThat(p.amount()).isEqualByComparingTo("3.33"));
    }

    private Long groupWithAllUsers() {
        Group group = Group.create("Viaje", null, null, ana, NOW);
        group.addMember(bea, NOW);
        group.addMember(carla, NOW);
        return groups.save(group).getId();
    }
}
