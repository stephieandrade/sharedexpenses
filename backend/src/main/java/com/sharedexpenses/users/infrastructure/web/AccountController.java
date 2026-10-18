package com.sharedexpenses.users.infrastructure.web;

import com.sharedexpenses.shared.security.CurrentUserId;
import com.sharedexpenses.users.application.ChangePasswordUseCase;
import com.sharedexpenses.users.application.DeleteAccountUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/me")
@Tag(name = "Cuenta", description = "Contraseña y eliminación de la cuenta del usuario actual")
public class AccountController {

    private final ChangePasswordUseCase changePassword;
    private final DeleteAccountUseCase deleteAccount;

    public AccountController(ChangePasswordUseCase changePassword, DeleteAccountUseCase deleteAccount) {
        this.changePassword = changePassword;
        this.deleteAccount = deleteAccount;
    }

    @PutMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Cambia la contraseña", description = "Requiere la contraseña actual")
    public void changePassword(@CurrentUserId Long userId, @Valid @RequestBody ChangePasswordRequest request) {
        changePassword.execute(userId, request.currentPassword(), request.newPassword());
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Elimina la cuenta",
            description = "Requiere la contraseña. Sale de todos los grupos (con balance 0) y anonimiza los datos "
                    + "personales; los gastos compartidos se conservan para el resto de miembros")
    public void deleteAccount(@CurrentUserId Long userId, @Valid @RequestBody DeleteAccountRequest request) {
        deleteAccount.execute(userId, request.password());
    }
}
