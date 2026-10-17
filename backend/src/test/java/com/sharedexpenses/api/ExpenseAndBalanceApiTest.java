package com.sharedexpenses.api;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ExpenseAndBalanceApiTest extends ApiTestSupport {

    TestUser ana;
    TestUser bea;
    TestUser carla;
    Long groupId;

    @BeforeEach
    void setUp() throws Exception {
        ana = registerUser("Ana");
        bea = registerUser("Bea");
        carla = registerUser("Carla");
        groupId = createGroup(ana, "Viaje", bea, carla);
    }

    @Test
    void fromExpensesToBalancesSettlementAndPayments() throws Exception {
        perform(bea, post("/api/groups/{id}/expenses", groupId), """
                {"description": "Cena", "amount": 10.00, "paidById": %d, "date": "2026-10-01", "splitType": "EQUAL",
                 "participants": [{"userId": %d}, {"userId": %d}, {"userId": %d}]}
                """, ana.id(), ana.id(), bea.id(), carla.id())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(10.00))
                .andExpect(jsonPath("$.date").value("2026-10-01"))
                .andExpect(jsonPath("$.shares[0].amount").value(3.34))
                .andExpect(jsonPath("$.shares[1].amount").value(3.33))
                .andExpect(jsonPath("$.shares[2].amount").value(3.33));

        getAs(carla, "/api/groups/{id}/balances", groupId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].amount").value(6.66))
                .andExpect(jsonPath("$[1].amount").value(-3.33))
                .andExpect(jsonPath("$[2].amount").value(-3.33));

        getAs(carla, "/api/groups/{id}/settlement", groupId)
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].fromUserId").value(bea.id()))
                .andExpect(jsonPath("$[0].toUserId").value(ana.id()))
                .andExpect(jsonPath("$[0].amount").value(3.33));

        perform(bea, post("/api/groups/{id}/payments", groupId), """
                {"fromUserId": %d, "toUserId": %d, "amount": 3.33}
                """, bea.id(), ana.id())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.date").isNotEmpty());

        getAs(ana, "/api/groups/{id}/settlement", groupId)
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].fromUserId").value(carla.id()));
    }

    @Test
    void percentageSplitsMustAddUpTo100() throws Exception {
        perform(ana, post("/api/groups/{id}/expenses", groupId), """
                {"description": "Hotel", "amount": 300, "paidById": %d, "splitType": "PERCENTAGES",
                 "participants": [{"userId": %d, "value": 50}, {"userId": %d, "value": 40}]}
                """, ana.id(), ana.id(), bea.id())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value("Los porcentajes suman 90 y deben sumar 100"));
    }

    @Test
    void participantsMustBeMembers() throws Exception {
        TestUser outsider = registerUser("Outsider");

        perform(ana, post("/api/groups/{id}/expenses", groupId), """
                {"description": "Taxi", "amount": 20, "paidById": %d, "splitType": "EQUAL",
                 "participants": [{"userId": %d}, {"userId": %d}]}
                """, ana.id(), ana.id(), outsider.id())
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void invalidExpenseDataIsABadRequest() throws Exception {
        perform(ana, post("/api/groups/{id}/expenses", groupId), """
                {"description": "", "amount": -5, "splitType": "EQUAL", "participants": []}
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.description").exists())
                .andExpect(jsonPath("$.errors.amount").exists())
                .andExpect(jsonPath("$.errors.paidById").exists())
                .andExpect(jsonPath("$.errors.participants").exists());
    }

    @Test
    void deletesAnExpense() throws Exception {
        JsonNode expense = read(perform(ana, post("/api/groups/{id}/expenses", groupId), """
                {"description": "Peaje", "amount": 12, "paidById": %d, "splitType": "AMOUNTS",
                 "participants": [{"userId": %d, "value": 8}, {"userId": %d, "value": 4}]}
                """, ana.id(), ana.id(), bea.id()).andExpect(status().isCreated()));

        deleteAs(carla, "/api/groups/{id}/expenses/{expenseId}", groupId, expense.get("id").asLong())
                .andExpect(status().isNoContent());
        getAs(ana, "/api/groups/{id}/expenses", groupId).andExpect(jsonPath("$").isEmpty());
        deleteAs(ana, "/api/groups/{id}/expenses/{expenseId}", groupId, expense.get("id").asLong())
                .andExpect(status().isNotFound());
    }
}
