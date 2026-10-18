package com.sharedexpenses.users.application;

import com.sharedexpenses.shared.domain.BusinessRuleException;
import com.sharedexpenses.users.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountUseCasesTest {

    private static final User ANA = new User(7L, "Ana", "ana@test.com", "old-hash");

    @Mock
    UserRepository users;
    @Mock
    PasswordHasher passwordHasher;
    @Mock
    MembershipRemover membershipRemover;

    @Test
    void changesThePasswordWhenTheCurrentOneIsCorrect() {
        when(users.findById(7L)).thenReturn(Optional.of(ANA));
        when(passwordHasher.matches("actual-123", "old-hash")).thenReturn(true);
        when(passwordHasher.hash("nueva-456")).thenReturn("new-hash");

        new ChangePasswordUseCase(users, passwordHasher).execute(7L, "actual-123", "nueva-456");

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(users).save(saved.capture());
        assertThat(saved.getValue().passwordHash()).isEqualTo("new-hash");
        assertThat(saved.getValue().email()).isEqualTo("ana@test.com");
    }

    @Test
    void rejectsAWrongCurrentPasswordOrAnInvalidNewOne() {
        when(users.findById(7L)).thenReturn(Optional.of(ANA));
        when(passwordHasher.matches("mala", "old-hash")).thenReturn(false);
        when(passwordHasher.matches("actual-123", "old-hash")).thenReturn(true);
        ChangePasswordUseCase changePassword = new ChangePasswordUseCase(users, passwordHasher);

        assertThatThrownBy(() -> changePassword.execute(7L, "mala", "nueva-456"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("actual");
        assertThatThrownBy(() -> changePassword.execute(7L, "actual-123", "corta"))
                .isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> changePassword.execute(7L, "actual-123", "actual-123"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("distinta");
        verify(users, never()).save(any());
    }

    @Test
    void deletingTheAccountLeavesTheGroupsAndAnonymizesTheUser() {
        when(users.findById(7L)).thenReturn(Optional.of(ANA));
        when(passwordHasher.matches("actual-123", "old-hash")).thenReturn(true);
        when(passwordHasher.hash(anyString())).thenReturn("unusable-hash");

        new DeleteAccountUseCase(users, passwordHasher, membershipRemover).execute(7L, "actual-123");

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        InOrder order = inOrder(membershipRemover, users);
        order.verify(membershipRemover).removeFromAllGroups(7L);
        order.verify(users).save(saved.capture());
        assertThat(saved.getValue().id()).isEqualTo(7L);
        assertThat(saved.getValue().name()).isEqualTo(User.DELETED_NAME);
        assertThat(saved.getValue().email()).isEqualTo("deleted-7@deleted.invalid");
        assertThat(saved.getValue().passwordHash()).isEqualTo("unusable-hash");
    }

    @Test
    void theAccountIsNotDeletedWithAWrongPasswordOrWhenTheGroupsDoNotAllowIt() {
        when(users.findById(7L)).thenReturn(Optional.of(ANA));
        when(passwordHasher.matches("mala", "old-hash")).thenReturn(false);
        when(passwordHasher.matches("actual-123", "old-hash")).thenReturn(true);
        doThrow(new BusinessRuleException("Salda tus deudas")).when(membershipRemover).removeFromAllGroups(7L);
        DeleteAccountUseCase deleteAccount = new DeleteAccountUseCase(users, passwordHasher, membershipRemover);

        assertThatThrownBy(() -> deleteAccount.execute(7L, "mala")).isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> deleteAccount.execute(7L, "actual-123"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("deudas");
        verify(users, never()).save(any());
    }
}
