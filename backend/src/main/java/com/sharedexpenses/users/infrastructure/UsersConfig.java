package com.sharedexpenses.users.infrastructure;

import com.sharedexpenses.shared.security.JwtProperties;
import com.sharedexpenses.users.application.LoginUseCase;
import com.sharedexpenses.users.application.PasswordHasher;
import com.sharedexpenses.users.application.RegisterUserUseCase;
import com.sharedexpenses.users.application.TokenIssuer;
import com.sharedexpenses.users.application.UserLookup;
import com.sharedexpenses.users.application.UserRepository;
import com.sharedexpenses.users.infrastructure.security.BCryptPasswordHasher;
import com.sharedexpenses.users.infrastructure.security.JwtTokenIssuer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;

import java.time.Clock;

@Configuration
public class UsersConfig {

    @Bean
    PasswordHasher passwordHasher(PasswordEncoder passwordEncoder) {
        return new BCryptPasswordHasher(passwordEncoder);
    }

    @Bean
    TokenIssuer tokenIssuer(JwtEncoder jwtEncoder, JwtProperties jwtProperties, Clock clock) {
        return new JwtTokenIssuer(jwtEncoder, jwtProperties, clock);
    }

    @Bean
    UserLookup userLookup(UserRepository users) {
        return new UserLookup(users);
    }

    @Bean
    RegisterUserUseCase registerUserUseCase(UserRepository users, PasswordHasher passwordHasher,
                                            TokenIssuer tokenIssuer) {
        return new RegisterUserUseCase(users, passwordHasher, tokenIssuer);
    }

    @Bean
    LoginUseCase loginUseCase(UserRepository users, PasswordHasher passwordHasher, TokenIssuer tokenIssuer) {
        return new LoginUseCase(users, passwordHasher, tokenIssuer);
    }
}
