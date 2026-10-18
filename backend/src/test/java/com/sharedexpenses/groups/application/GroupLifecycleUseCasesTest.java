package com.sharedexpenses.groups.application;

import com.sharedexpenses.groups.domain.Group;
import com.sharedexpenses.shared.domain.BusinessRuleException;
import com.sharedexpenses.shared.domain.ForbiddenException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GroupLifecycleUseCasesTest {

    private static final Instant NOW = Instant.parse("2026-10-18T10:00:00Z");

    @Mock
    GroupRepository groups;
    @Mock
    MemberBalanceProvider balances;

    @Test
    void anyMemberCanRenameTheGroupButTheCurrencyStays() {
        Group group = group(10L, "Piso", 1L, 2L);
        when(groups.findById(10L)).thenReturn(Optional.of(group));
        when(groups.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Group updated = new UpdateGroupUseCase(new GroupAccess(groups), groups)
                .execute(new UpdateGroupUseCase.Command(10L, 2L, "Piso de Gràcia", "Gastos comunes"));

        assertThat(updated.getName()).isEqualTo("Piso de Gràcia");
        assertThat(updated.getDescription()).isEqualTo("Gastos comunes");
        assertThat(updated.getCurrency()).isEqualTo("EUR");
        assertThat(updated.getMemberIds()).containsExactlyInAnyOrder(1L, 2L);
        assertThat(updated.getVersion()).isEqualTo(group.getVersion());
    }

    @Test
    void onlyTheCreatorCanDeleteTheGroup() {
        when(groups.findById(10L)).thenReturn(Optional.of(group(10L, "Piso", 1L, 2L)));
        DeleteGroupUseCase deleteGroup = new DeleteGroupUseCase(new GroupAccess(groups), groups);

        assertThatThrownBy(() -> deleteGroup.execute(10L, 2L)).isInstanceOf(ForbiddenException.class);
        verify(groups, never()).deleteById(any());

        deleteGroup.execute(10L, 1L);
        verify(groups).deleteById(10L);
    }

    @Test
    void leavingAllGroupsRemovesTheUserAndDeletesTheGroupsWhereTheyAreAlone() {
        Group shared = group(10L, "Piso", 1L, 7L);
        Group alone = group(20L, "Ahorro", 7L);
        when(groups.findByMember(7L)).thenReturn(List.of(shared, alone));
        when(balances.balanceOf(10L, 7L)).thenReturn(new BigDecimal("0.00"));

        new LeaveAllGroupsUseCase(groups, balances).execute(7L);

        ArgumentCaptor<Group> saved = ArgumentCaptor.forClass(Group.class);
        verify(groups).save(saved.capture());
        assertThat(saved.getValue().getMemberIds()).containsExactly(1L);
        verify(groups).deleteById(20L);
    }

    @Test
    void cannotLeaveIfTheUserCreatedAGroupWithOtherMembers() {
        when(groups.findByMember(1L)).thenReturn(List.of(group(10L, "Piso", 1L, 2L)));

        assertThatThrownBy(() -> new LeaveAllGroupsUseCase(groups, balances).execute(1L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Piso");
        verify(groups, never()).deleteById(any());
        verify(groups, never()).save(any());
    }

    @Test
    void cannotLeaveWithAPendingBalanceAndNothingIsChanged() {
        Group alone = group(20L, "Ahorro", 7L);
        Group debts = group(10L, "Viaje", 1L, 7L);
        when(groups.findByMember(7L)).thenReturn(List.of(alone, debts));
        when(balances.balanceOf(10L, 7L)).thenReturn(new BigDecimal("-12.00"));

        assertThatThrownBy(() -> new LeaveAllGroupsUseCase(groups, balances).execute(7L))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Viaje");
        verify(groups, never()).deleteById(any());
        verify(groups, never()).save(any());
    }

    private static Group group(Long id, String name, Long creatorId, Long... otherMembers) {
        Group group = new Group(id, name, null, "EUR", creatorId, NOW, List.of(), 3L);
        group.addMember(creatorId, NOW);
        for (Long member : otherMembers) {
            group.addMember(member, NOW);
        }
        return group;
    }
}
