package com.sharedexpenses.api;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AccountApiTest extends ApiTestSupport {

    private static final String NEW_PASSWORD = "otra-clave-segura";

    @Test
    void changesThePasswordAndOnlyTheNewOneWorks() throws Exception {
        TestUser ana = registerUser("Ana");

        perform(ana, put("/api/users/me/password"), """
                {"currentPassword": "%s", "newPassword": "%s"}
                """, PASSWORD, NEW_PASSWORD).andExpect(status().isNoContent());

        login(ana.email(), PASSWORD).andExpect(status().isUnauthorized());
        login(ana.email(), NEW_PASSWORD).andExpect(status().isOk());
    }

    @Test
    void aWrongCurrentPasswordIsABusinessErrorNotAnExpiredSession() throws Exception {
        TestUser ana = registerUser("Ana");

        perform(ana, put("/api/users/me/password"), """
                {"currentPassword": "no-es-esta", "newPassword": "%s"}
                """, NEW_PASSWORD)
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value("La contraseña actual no es correcta"));
    }

    @Test
    void deletingTheAccountLeavesTheGroupsKeepsTheHistoryAndBlocksTheLogin() throws Exception {
        TestUser ana = registerUser("Ana");
        TestUser bea = registerUser("Bea");
        Long groupId = createGroup(ana, "Piso", bea);
        perform(ana, post("/api/groups/{id}/expenses", groupId), """
                {"description": "Compra", "amount": 20, "paidById": %d, "splitType": "EQUAL",
                 "participants": [{"userId": %d}, {"userId": %d}]}
                """, ana.id(), ana.id(), bea.id()).andExpect(status().isCreated());
        perform(bea, post("/api/groups/{id}/payments", groupId), """
                {"fromUserId": %d, "toUserId": %d, "amount": 10}
                """, bea.id(), ana.id()).andExpect(status().isCreated());

        perform(bea, delete("/api/users/me"), """
                {"password": "%s"}
                """, PASSWORD).andExpect(status().isNoContent());

        login(bea.email(), PASSWORD).andExpect(status().isUnauthorized());
        getAs(ana, "/api/groups/{id}", groupId).andExpect(jsonPath("$.members.length()").value(1));
        getAs(ana, "/api/groups/{id}/expenses", groupId).andExpect(jsonPath("$[0].shares.length()").value(2));
        getAs(ana, "/api/groups/{id}/balances", groupId).andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void theAccountCannotBeDeletedWithDebtsOrOwnGroupsWithMembersOrAWrongPassword() throws Exception {
        TestUser ana = registerUser("Ana");
        TestUser bea = registerUser("Bea");
        Long groupId = createGroup(ana, "Piso", bea);
        perform(ana, post("/api/groups/{id}/expenses", groupId), """
                {"description": "Compra", "amount": 20, "paidById": %d, "splitType": "EQUAL",
                 "participants": [{"userId": %d}, {"userId": %d}]}
                """, ana.id(), ana.id(), bea.id()).andExpect(status().isCreated());

        perform(bea, delete("/api/users/me"), """
                {"password": "%s"}
                """, PASSWORD)
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value(
                        "Tienes un balance pendiente en \"Piso\". Salda tus deudas antes de borrar tu cuenta"));
        perform(ana, delete("/api/users/me"), """
                {"password": "%s"}
                """, PASSWORD).andExpect(status().isUnprocessableEntity());
        perform(ana, delete("/api/users/me"), """
                {"password": "incorrecta"}
                """).andExpect(status().isUnprocessableEntity());

        login(bea.email(), PASSWORD).andExpect(status().isOk());
        getAs(ana, "/api/groups/{id}", groupId).andExpect(jsonPath("$.members.length()").value(2));
    }

    private org.springframework.test.web.servlet.ResultActions login(String email, String password) throws Exception {
        return mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"email": "%s", "password": "%s"}
                        """.formatted(email, password)));
    }
}
