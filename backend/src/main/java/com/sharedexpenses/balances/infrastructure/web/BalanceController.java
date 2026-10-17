package com.sharedexpenses.balances.infrastructure.web;

import com.sharedexpenses.balances.application.CalculateBalancesUseCase;
import com.sharedexpenses.balances.application.GetSuggestedSettlementUseCase;
import com.sharedexpenses.balances.application.RegisterPaymentUseCase;
import com.sharedexpenses.shared.security.CurrentUserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/groups/{groupId}")
@Tag(name = "Balances", description = "Balances, liquidación sugerida y pagos entre miembros")
public class BalanceController {

    private final CalculateBalancesUseCase calculateBalances;
    private final GetSuggestedSettlementUseCase getSuggestedSettlement;
    private final RegisterPaymentUseCase registerPayment;

    public BalanceController(CalculateBalancesUseCase calculateBalances,
                             GetSuggestedSettlementUseCase getSuggestedSettlement,
                             RegisterPaymentUseCase registerPayment) {
        this.calculateBalances = calculateBalances;
        this.getSuggestedSettlement = getSuggestedSettlement;
        this.registerPayment = registerPayment;
    }

    @GetMapping("/balances")
    @Operation(summary = "Balance de cada miembro", description = "La suma de todos los balances es siempre 0")
    public List<BalanceResponse> balances(@CurrentUserId Long userId, @PathVariable Long groupId) {
        return calculateBalances.execute(groupId, userId).stream().map(BalanceResponse::from).toList();
    }

    @GetMapping("/settlement")
    @Operation(summary = "Liquidación sugerida",
            description = "Pagos mínimos para saldar todas las deudas (como mucho N−1 pagos)")
    public List<SuggestedPaymentResponse> settlement(@CurrentUserId Long userId, @PathVariable Long groupId) {
        return getSuggestedSettlement.execute(groupId, userId).stream().map(SuggestedPaymentResponse::from).toList();
    }

    @PostMapping("/payments")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registra un pago entre dos miembros")
    public PaymentResponse registerPayment(@CurrentUserId Long userId, @PathVariable Long groupId,
                                           @Valid @RequestBody RegisterPaymentRequest request) {
        return PaymentResponse.from(registerPayment.execute(new RegisterPaymentUseCase.Command(
                groupId, userId, request.fromUserId(), request.toUserId(), request.amount(), request.date())));
    }
}
