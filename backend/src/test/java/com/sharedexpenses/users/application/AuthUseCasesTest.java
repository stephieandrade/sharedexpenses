package com.sharedexpenses.users.application;

import com.sharedexpenses.shared.domain.BusinessRuleException;
import com.sharedexpenses.shared.domain.ConflictException;
import com.sharedexpenses.shared.domain.UnauthorizedException;
import com.sharedexpenses.users.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthUseCasesTest {

    private static final IssuedToken TOKEN = new IssuedToken("jwt", Instant.parse("2026-10-18T10:00:00Z"));

    @Mock
    UserRepository users;
    @Mock
    PasswordHasher passwordHasher;
    @Mock
    TokenIssuer tokenIssuer;

    RegisterUserUseCase registerUser;
    LoginUseCase login;

    @BeforeEach
    void setUp() {
        registerUser = new RegisterUserUseCase(users, passwordHasher, tokenIssuer);
        login = new LoginUseCase(users, passwordHasher, tokenIssuer);
    }

    @Test
    void registerStoresTheHashedPasswordAndReturnsAToken() {
        when(users.findByEmail("ana@test.com")).thenReturn(Optional.empty());
        when(passwordHasher.hash("secreta123")).thenReturn("hashed");
        when(users.save(any())).thenAnswer(invocation -> withId(invocation.getArgument(0), 7L));
        when(tokenIssuer.issue(any())).thenReturn(TOKEN);

        AuthenticatedUser result = registerUser.execute(new RegisterUserUseCase.Command("Ana", " Ana@Test.com ", "secreta123"));

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(users).save(saved.capture());
        assertThat(saved.getValue().passwordHash()).isEqualTo("hashed");
        assertThat(saved.getValue().email()).isEqualTo("ana@test.com");
        assertThat(result.user()).isEqualTo(new UserSummary(7L, "Ana", "ana@test.com"));
        assertThat(result.token()).isEqualTo(TOKEN);
    }

    @Test
    void registerRejectsAnEmailAlreadyInUse() {
        when(users.findByEmail("ana@test.com")).thenReturn(Optional.of(new User(1L, "Ana", "ana@test.com", "h")));

        assertThatThrownBy(() -> registerUser.execute(new RegisterUserUseCase.Command("Ana", "ana@test.com", "secreta123")))
                .isInstanceOf(ConflictException.class);
        verify(users, never()).save(any());
    }

    @Test
    void registerRejectsShortPasswords() {
        assertThatThrownBy(() -> registerUser.execute(new RegisterUserUseCase.Command("Ana", "ana@test.com", "1234567")))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void loginReturnsATokenForValidCredentials() {
        User ana = new User(1L, "Ana", "ana@test.com", "hashed");
        when(users.findByEmail("ana@test.com")).thenReturn(Optional.of(ana));
        when(passwordHasher.matches("secreta123", "hashed")).thenReturn(true);
        when(tokenIssuer.issue(ana)).thenReturn(TOKEN);

        assertThat(login.execute("ANA@test.com", "secreta123").token()).isEqualTo(TOKEN);
    }

    @Test
    void loginFailsWithAWrongPasswordOrUnknownEmail() {
        when(users.findByEmail("ana@test.com")).thenReturn(Optional.of(new User(1L, "Ana", "ana@test.com", "hashed")));
        when(passwordHasher.matches("mala", "hashed")).thenReturn(false);
        when(users.findByEmail("nadie@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> login.execute("ana@test.com", "mala")).isInstanceOf(UnauthorizedException.class);
        assertThatThrownBy(() -> login.execute("nadie@test.com", "mala")).isInstanceOf(UnauthorizedException.class);
        verify(tokenIssuer, never()).issue(any());
    }

    private static User withId(User user, Long id) {
        return new User(id, user.name(), user.email(), user.passwordHash());
    }
}
