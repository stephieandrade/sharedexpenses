package com.sharedexpenses.users.infrastructure.web;

import com.sharedexpenses.users.application.LoginUseCase;
import com.sharedexpenses.users.application.RegisterUserUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticación", description = "Registro e inicio de sesión")
@SecurityRequirements
public class AuthController {

    private final RegisterUserUseCase registerUser;
    private final LoginUseCase login;

    public AuthController(RegisterUserUseCase registerUser, LoginUseCase login) {
        this.registerUser = registerUser;
        this.login = login;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registra un usuario", description = "Crea la cuenta y devuelve un token JWT para usarla directamente")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return AuthResponse.from(registerUser.execute(
                new RegisterUserUseCase.Command(request.name(), request.email(), request.password())));
    }

    @PostMapping("/login")
    @Operation(summary = "Inicia sesión", description = "Devuelve un token JWT a enviar en la cabecera Authorization: Bearer")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return AuthResponse.from(login.execute(request.email(), request.password()));
    }
}
