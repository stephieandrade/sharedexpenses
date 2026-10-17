package com.sharedexpenses.groups.application;

import com.sharedexpenses.groups.domain.Group;
import com.sharedexpenses.shared.domain.ForbiddenException;
import com.sharedexpenses.shared.domain.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GroupAccessTest {

    @Mock
    GroupRepository groups;

    @InjectMocks
    GroupAccess access;

    @Test
    void returnsTheGroupToAMember() {
        Group group = Group.create("Piso", null, null, 1L, Instant.now());
        when(groups.findById(10L)).thenReturn(Optional.of(group));

        assertThat(access.requireMember(10L, 1L)).isSameAs(group);
    }

    @Test
    void rejectsNonMembers() {
        when(groups.findById(10L)).thenReturn(Optional.of(Group.create("Piso", null, null, 1L, Instant.now())));

        assertThatThrownBy(() -> access.requireMember(10L, 99L)).isInstanceOf(ForbiddenException.class);
    }

    @Test
    void failsWhenTheGroupDoesNotExist() {
        when(groups.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> access.requireMember(10L, 1L)).isInstanceOf(NotFoundException.class);
    }
}
