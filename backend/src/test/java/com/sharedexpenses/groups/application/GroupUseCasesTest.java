package com.sharedexpenses.groups.application;

import com.sharedexpenses.groups.domain.Group;
import com.sharedexpenses.shared.domain.BusinessRuleException;
import com.sharedexpenses.shared.domain.ForbiddenException;
import com.sharedexpenses.shared.domain.NotFoundException;
import com.sharedexpenses.users.application.UserLookup;
import com.sharedexpenses.users.application.UserSummary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
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
class GroupUseCasesTest {

    private static final Instant NOW = Instant.parse("2026-10-17T10:00:00Z");
    private final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);

    @Mock
    GroupRepository groups;
    @Mock
    UserLookup users;
    @Mock
    MemberBalanceProvider balances;

    GroupAccess access;
    Group group;

    @BeforeEach
    void setUp() {
        access = new GroupAccess(groups);
        group = new Group(10L, "Piso", null, "EUR", 1L, NOW, List.of(), 0L);
        group.addMember(1L, NOW);
        group.addMember(2L, NOW);
    }

    @Test
    void createGroupSavesItWithTheCreatorAsMember() {
        when(groups.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Group created = new CreateGroupUseCase(groups, clock)
                .execute(new CreateGroupUseCase.Command("Viaje", "Lisboa", null, 1L));

        assertThat(created.getMemberIds()).containsExactly(1L);
        assertThat(created.getCreatedAt()).isEqualTo(NOW);
        assertThat(created.getCurrency()).isEqualTo("EUR");
    }

    @Test
    void groupDetailListsMembersInJoiningOrder() {
        when(groups.findById(10L)).thenReturn(Optional.of(group));
        when(users.findByIds(any())).thenReturn(List.of(
                new UserSummary(2L, "Bea", "bea@test.com"), new UserSummary(1L, "Ana", "ana@test.com")));

        GroupDetail detail = new GetGroupDetailUseCase(access, users).execute(10L, 1L);

        assertThat(detail.members()).extracting(UserSummary::name).containsExactly("Ana", "Bea");
    }

    @Test
    void addMemberFindsTheUserByEmailAndSavesTheGroup() {
        when(groups.findById(10L)).thenReturn(Optional.of(group));
        when(users.findByEmail("carla@test.com")).thenReturn(Optional.of(new UserSummary(3L, "Carla", "carla@test.com")));

        UserSummary added = new AddMemberUseCase(access, groups, users, clock).execute(10L, 1L, "carla@test.com");

        ArgumentCaptor<Group> saved = ArgumentCaptor.forClass(Group.class);
        verify(groups).save(saved.capture());
        assertThat(saved.getValue().getMemberIds()).containsExactlyInAnyOrder(1L, 2L, 3L);
        assertThat(added.id()).isEqualTo(3L);
    }

    @Test
    void addMemberFailsForAnUnknownEmail() {
        when(groups.findById(10L)).thenReturn(Optional.of(group));
        when(users.findByEmail("nadie@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> new AddMemberUseCase(access, groups, users, clock).execute(10L, 1L, "nadie@test.com"))
                .isInstanceOf(NotFoundException.class);
        verify(groups, never()).save(any());
    }

    @Test
    void onlyMembersCanAddMembers() {
        when(groups.findById(10L)).thenReturn(Optional.of(group));

        assertThatThrownBy(() -> new AddMemberUseCase(access, groups, users, clock).execute(10L, 99L, "x@test.com"))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void removeMemberWithZeroBalance() {
        when(groups.findById(10L)).thenReturn(Optional.of(group));
        when(balances.balanceOf(10L, 2L)).thenReturn(new BigDecimal("0.00"));

        new RemoveMemberUseCase(access, groups, balances).execute(10L, 1L, 2L);

        ArgumentCaptor<Group> saved = ArgumentCaptor.forClass(Group.class);
        verify(groups).save(saved.capture());
        assertThat(saved.getValue().getMemberIds()).containsExactly(1L);
    }

    @Test
    void removeMemberWithPendingBalanceIsRejected() {
        when(groups.findById(10L)).thenReturn(Optional.of(group));
        when(balances.balanceOf(10L, 2L)).thenReturn(new BigDecimal("-12.50"));

        assertThatThrownBy(() -> new RemoveMemberUseCase(access, groups, balances).execute(10L, 1L, 2L))
                .isInstanceOf(BusinessRuleException.class);
        verify(groups, never()).save(any());
    }

    @Test
    void aMemberCanLeaveButCannotRemoveOthers() {
        group.addMember(3L, NOW);
        when(groups.findById(10L)).thenReturn(Optional.of(group));
        when(balances.balanceOf(10L, 2L)).thenReturn(new BigDecimal("0.00"));
        RemoveMemberUseCase removeMember = new RemoveMemberUseCase(access, groups, balances);

        assertThatThrownBy(() -> removeMember.execute(10L, 2L, 3L)).isInstanceOf(ForbiddenException.class);
        removeMember.execute(10L, 2L, 2L);

        ArgumentCaptor<Group> saved = ArgumentCaptor.forClass(Group.class);
        verify(groups).save(saved.capture());
        assertThat(saved.getValue().getMemberIds()).containsExactlyInAnyOrder(1L, 3L);
    }

    @Test
    void removeMemberFailsIfTheUserIsNotInTheGroup() {
        when(groups.findById(10L)).thenReturn(Optional.of(group));

        assertThatThrownBy(() -> new RemoveMemberUseCase(access, groups, balances).execute(10L, 1L, 7L))
                .isInstanceOf(NotFoundException.class);
        verify(balances, never()).balanceOf(any(), any());
    }
}
