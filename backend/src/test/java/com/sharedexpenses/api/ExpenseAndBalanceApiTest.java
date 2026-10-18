package com.sharedexpenses.api;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
    void anExpenseCanBeEditedAndTheBalancesFollow() throws Exception {
        JsonNode expense = read(perform(ana, post("/api/groups/{id}/expenses", groupId), """
                {"description": "Cena", "amount": 30, "paidById": %d, "date": "2026-10-01", "splitType": "EQUAL",
                 "participants": [{"userId": %d}, {"userId": %d}, {"userId": %d}]}
                """, ana.id(), ana.id(), bea.id(), carla.id()).andExpect(status().isCreated()));
        long expenseId = expense.get("id").asLong();

        perform(bea, put("/api/groups/{id}/expenses/{expenseId}", groupId, expenseId), """
                {"description": "Cena con postre", "amount": 40, "paidById": %d, "splitType": "PERCENTAGES",
                 "participants": [{"userId": %d, "value": 75}, {"userId": %d, "value": 25}]}
                """, bea.id(), ana.id(), bea.id())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(expenseId))
                .andExpect(jsonPath("$.description").value("Cena con postre"))
                .andExpect(jsonPath("$.date").value("2026-10-01"))
                .andExpect(jsonPath("$.splitType").value("PERCENTAGES"))
                .andExpect(jsonPath("$.shares.length()").value(2))
                .andExpect(jsonPath("$.shares[0].percentage").value(75.0));

        getAs(ana, "/api/groups/{id}/expenses", groupId)
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].amount").value(40.0));
        getAs(ana, "/api/groups/{id}/balances", groupId)
                .andExpect(jsonPath("$[0].amount").value(-30.0))
                .andExpect(jsonPath("$[1].amount").value(30.0))
                .andExpect(jsonPath("$[2].amount").value(0.0));
    }

    @Test
    void invalidEditsAreRejected() throws Exception {
        TestUser outsider = registerUser("Outsider");
        JsonNode expense = read(perform(ana, post("/api/groups/{id}/expenses", groupId), """
                {"description": "Cena", "amount": 30, "paidById": %d, "splitType": "EQUAL",
                 "participants": [{"userId": %d}, {"userId": %d}]}
                """, ana.id(), ana.id(), bea.id()).andExpect(status().isCreated()));
        long expenseId = expense.get("id").asLong();
        String validBody = """
                {"description": "Cena", "amount": 30, "paidById": %d, "splitType": "EQUAL",
                 "participants": [{"userId": %d}]}
                """;

        perform(ana, put("/api/groups/{id}/expenses/{expenseId}", groupId, expenseId), """
                {"description": "Cena", "amount": 30, "paidById": %d, "splitType": "AMOUNTS",
                 "participants": [{"userId": %d, "value": 10}, {"userId": %d, "value": 10}]}
                """, ana.id(), ana.id(), bea.id()).andExpect(status().isUnprocessableEntity());
        perform(outsider, put("/api/groups/{id}/expenses/{expenseId}", groupId, expenseId), validBody,
                outsider.id(), outsider.id()).andExpect(status().isForbidden());
        perform(ana, put("/api/groups/{id}/expenses/{expenseId}", groupId, 999_999), validBody,
                ana.id(), ana.id()).andExpect(status().isNotFound());
        getAs(ana, "/api/groups/{id}/expenses", groupId).andExpect(jsonPath("$[0].shares.length()").value(2));
    }

    @Test
    void paymentsCanBeListedAndDeleted() throws Exception {
        perform(ana, post("/api/groups/{id}/expenses", groupId), """
                {"description": "Cena", "amount": 30, "paidById": %d, "splitType": "EQUAL",
                 "participants": [{"userId": %d}, {"userId": %d}, {"userId": %d}]}
                """, ana.id(), ana.id(), bea.id(), carla.id()).andExpect(status().isCreated());
        JsonNode payment = read(perform(bea, post("/api/groups/{id}/payments", groupId), """
                {"fromUserId": %d, "toUserId": %d, "amount": 10}
                """, bea.id(), ana.id()).andExpect(status().isCreated()));

        getAs(carla, "/api/groups/{id}/payments", groupId)
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].fromUserId").value(bea.id()));

        deleteAs(carla, "/api/groups/{id}/payments/{paymentId}", groupId, payment.get("id").asLong())
                .andExpect(status().isNoContent());
        getAs(ana, "/api/groups/{id}/payments", groupId).andExpect(jsonPath("$").isEmpty());
        getAs(ana, "/api/groups/{id}/balances", groupId).andExpect(jsonPath("$[1].amount").value(-10.0));
    }

    @Test
    void deletingTheLastExpenseClearsThePaymentsAndTheBalances() throws Exception {
        JsonNode expense = read(perform(ana, post("/api/groups/{id}/expenses", groupId), """
                {"description": "Taxi", "amount": 15, "paidById": %d, "splitType": "EQUAL",
                 "participants": [{"userId": %d}, {"userId": %d}, {"userId": %d}]}
                """, ana.id(), ana.id(), bea.id(), carla.id()).andExpect(status().isCreated()));
        perform(bea, post("/api/groups/{id}/payments", groupId), """
                {"fromUserId": %d, "toUserId": %d, "amount": 5}
                """, bea.id(), ana.id()).andExpect(status().isCreated());

        deleteAs(ana, "/api/groups/{id}/expenses/{expenseId}", groupId, expense.get("id").asLong())
                .andExpect(status().isNoContent());

        getAs(ana, "/api/groups/{id}/payments", groupId).andExpect(jsonPath("$").isEmpty());
        getAs(ana, "/api/groups/{id}/settlement", groupId).andExpect(jsonPath("$").isEmpty());
        getAs(ana, "/api/groups/{id}/balances", groupId)
                .andExpect(jsonPath("$[0].amount").value(0.0))
                .andExpect(jsonPath("$[1].amount").value(0.0))
                .andExpect(jsonPath("$[2].amount").value(0.0));
    }

    @Test
    void deletingAnExpenseKeepsPaymentsWhileOtherExpensesRemain() throws Exception {
        JsonNode taxi = read(perform(ana, post("/api/groups/{id}/expenses", groupId), """
                {"description": "Taxi", "amount": 15, "paidById": %d, "splitType": "EQUAL",
                 "participants": [{"userId": %d}, {"userId": %d}, {"userId": %d}]}
                """, ana.id(), ana.id(), bea.id(), carla.id()).andExpect(status().isCreated()));
        perform(ana, post("/api/groups/{id}/expenses", groupId), """
                {"description": "Hotel", "amount": 90, "paidById": %d, "splitType": "EQUAL",
                 "participants": [{"userId": %d}, {"userId": %d}, {"userId": %d}]}
                """, ana.id(), ana.id(), bea.id(), carla.id()).andExpect(status().isCreated());
        perform(bea, post("/api/groups/{id}/payments", groupId), """
                {"fromUserId": %d, "toUserId": %d, "amount": 20}
                """, bea.id(), ana.id()).andExpect(status().isCreated());

        deleteAs(ana, "/api/groups/{id}/expenses/{expenseId}", groupId, taxi.get("id").asLong())
                .andExpect(status().isNoContent());

        getAs(ana, "/api/groups/{id}/payments", groupId).andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void paymentEndpointsAreOnlyForMembers() throws Exception {
        TestUser outsider = registerUser("Outsider");

        getAs(outsider, "/api/groups/{id}/payments", groupId).andExpect(status().isForbidden());
        deleteAs(outsider, "/api/groups/{id}/payments/{paymentId}", groupId, 1).andExpect(status().isForbidden());
        deleteAs(ana, "/api/groups/{id}/payments/{paymentId}", groupId, 999_999).andExpect(status().isNotFound());
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
