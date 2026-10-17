package com.sharedexpenses.groups.domain;

import com.sharedexpenses.shared.domain.BusinessRuleException;
import com.sharedexpenses.shared.domain.ForbiddenException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GroupTest {

    private static final Instant NOW = Instant.parse("2026-10-17T10:00:00Z");

    @Test
    void creatorIsTheFirstMemberAndDefaultCurrencyIsEur() {
        Group group = Group.create("Viaje a Lisboa", null, null, 1L, NOW);

        assertThat(group.isMember(1L)).isTrue();
        assertThat(group.getMembers()).hasSize(1);
        assertThat(group.getCurrency()).isEqualTo("EUR");
    }

    @Test
    void addsMembersWithoutDuplicates() {
        Group group = Group.create("Piso", null, "eur", 1L, NOW);
        group.addMember(2L, NOW);

        assertThat(group.getMemberIds()).containsExactlyInAnyOrder(1L, 2L);
        assertThatThrownBy(() -> group.addMember(2L, NOW))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("ya es miembro");
    }

    @Test
    void requireMembersFailsIfSomeoneDoesNotBelong() {
        Group group = Group.create("Piso", null, null, 1L, NOW);

        assertThatThrownBy(() -> group.requireMembers(List.of(1L, 7L)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("7");
    }

    @Test
    void onlyRemovesAMemberWithZeroBalance() {
        Group group = groupWithMembers(2L);

        assertThatThrownBy(() -> group.removeMember(2L, 1L, new BigDecimal("-5.00")))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("balance");

        group.removeMember(2L, 1L, new BigDecimal("0.00"));
        assertThat(group.isMember(2L)).isFalse();
    }

    @Test
    void aMemberCanLeaveTheGroup() {
        Group group = groupWithMembers(2L);

        group.removeMember(2L, 2L, BigDecimal.ZERO);

        assertThat(group.getMemberIds()).containsExactly(1L);
    }

    @Test
    void onlyTheCreatorCanRemoveOtherMembers() {
        Group group = groupWithMembers(2L, 3L);

        assertThatThrownBy(() -> group.removeMember(3L, 2L, BigDecimal.ZERO))
                .isInstanceOf(ForbiddenException.class);
        assertThat(group.isMember(3L)).isTrue();
    }

    @Test
    void theCreatorCannotBeRemoved() {
        Group group = groupWithMembers(2L);

        assertThatThrownBy(() -> group.removeMember(1L, 1L, BigDecimal.ZERO))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("creador");
    }

    private static Group groupWithMembers(Long... userIds) {
        Group group = Group.create("Piso", null, null, 1L, NOW);
        for (Long userId : userIds) {
            group.addMember(userId, NOW);
        }
        return group;
    }

    @Test
    void rejectsBlankNameAndInvalidCurrency() {
        assertThatThrownBy(() -> Group.create(" ", null, null, 1L, NOW))
                .isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> Group.create("Piso", null, "euros", 1L, NOW))
                .isInstanceOf(BusinessRuleException.class);
    }
}
