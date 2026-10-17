package com.sharedexpenses.expenses.infrastructure.web;

import com.sharedexpenses.expenses.application.CreateExpenseUseCase;
import com.sharedexpenses.expenses.application.DeleteExpenseUseCase;
import com.sharedexpenses.expenses.application.ListExpensesUseCase;
import com.sharedexpenses.expenses.domain.Participant;
import com.sharedexpenses.shared.security.CurrentUserId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/groups/{groupId}/expenses")
@Tag(name = "Gastos", description = "Gastos de un grupo y cómo se reparten")
public class ExpenseController {

    private final ListExpensesUseCase listExpenses;
    private final CreateExpenseUseCase createExpense;
    private final DeleteExpenseUseCase deleteExpense;

    public ExpenseController(ListExpensesUseCase listExpenses, CreateExpenseUseCase createExpense,
                             DeleteExpenseUseCase deleteExpense) {
        this.listExpenses = listExpenses;
        this.createExpense = createExpense;
        this.deleteExpense = deleteExpense;
    }

    @GetMapping
    @Operation(summary = "Lista los gastos del grupo", description = "Los más recientes primero")
    public List<ExpenseResponse> list(@CurrentUserId Long userId, @PathVariable Long groupId) {
        return listExpenses.execute(groupId, userId).stream().map(ExpenseResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registra un gasto",
            description = "EQUAL reparte a partes iguales, AMOUNTS por importes y PERCENTAGES por porcentajes. "
                    + "Las partes suman siempre el importe exacto")
    public ExpenseResponse create(@CurrentUserId Long userId, @PathVariable Long groupId,
                                  @Valid @RequestBody CreateExpenseRequest request) {
        List<Participant> participants = request.participants().stream()
                .map(p -> new Participant(p.userId(), p.value()))
                .toList();
        return ExpenseResponse.from(createExpense.execute(new CreateExpenseUseCase.Command(
                groupId, userId, request.description(), request.amount(), request.paidById(), request.date(),
                request.splitType(), participants)));
    }

    @DeleteMapping("/{expenseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Elimina un gasto")
    public void delete(@CurrentUserId Long userId, @PathVariable Long groupId, @PathVariable Long expenseId) {
        deleteExpense.execute(groupId, expenseId, userId);
    }
}
