package com.sharedexpenses.api;

import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GroupApiTest extends ApiTestSupport {

    @Test
    void createsAGroupWithTheCreatorAsMember() throws Exception {
        TestUser ana = registerUser("Ana");

        perform(ana, post("/api/groups"), """
                {"name": "Viaje a Roma", "description": "Puente de mayo", "currency": "eur"}
                """)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Viaje a Roma"))
                .andExpect(jsonPath("$.currency").value("EUR"))
                .andExpect(jsonPath("$.creatorId").value(ana.id()))
                .andExpect(jsonPath("$.memberCount").value(1));

        getAs(ana, "/api/groups")
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Viaje a Roma"));
    }

    @Test
    void addsMembersByEmailAndShowsThemInTheDetail() throws Exception {
        TestUser ana = registerUser("Ana");
        TestUser bea = registerUser("Bea");
        Long groupId = createGroup(ana, "Piso", bea);

        getAs(ana, "/api/groups/{id}", groupId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.members.length()").value(2))
                .andExpect(jsonPath("$.members[0].name").value("Ana"))
                .andExpect(jsonPath("$.members[1].email").value(bea.email()));
        getAs(bea, "/api/groups").andExpect(jsonPath("$[0].id").value(groupId));
    }

    @Test
    void addingAnUnknownEmailOrAnExistingMemberFails() throws Exception {
        TestUser ana = registerUser("Ana");
        TestUser bea = registerUser("Bea");
        Long groupId = createGroup(ana, "Piso", bea);

        perform(ana, post("/api/groups/{id}/members", groupId), """
                {"email": "nadie@test.com"}
                """).andExpect(status().isNotFound());
        perform(ana, post("/api/groups/{id}/members", groupId), """
                {"email": "%s"}
                """, bea.email())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value("El usuario ya es miembro del grupo"));
    }

    @Test
    void onlyTheCreatorCanRemoveOthersButAnyoneCanLeave() throws Exception {
        TestUser ana = registerUser("Ana");
        TestUser bea = registerUser("Bea");
        TestUser carla = registerUser("Carla");
        Long groupId = createGroup(ana, "Piso", bea, carla);

        deleteAs(bea, "/api/groups/{id}/members/{userId}", groupId, carla.id()).andExpect(status().isForbidden());
        deleteAs(bea, "/api/groups/{id}/members/{userId}", groupId, bea.id()).andExpect(status().isNoContent());
        deleteAs(ana, "/api/groups/{id}/members/{userId}", groupId, carla.id()).andExpect(status().isNoContent());
        deleteAs(ana, "/api/groups/{id}/members/{userId}", groupId, ana.id())
                .andExpect(status().isUnprocessableEntity());

        getAs(ana, "/api/groups/{id}", groupId).andExpect(jsonPath("$.members.length()").value(1));
        getAs(bea, "/api/groups/{id}", groupId).andExpect(status().isForbidden());
    }

    @Test
    void aMemberWithPendingBalanceCannotBeRemoved() throws Exception {
        TestUser ana = registerUser("Ana");
        TestUser bea = registerUser("Bea");
        Long groupId = createGroup(ana, "Piso", bea);
        perform(ana, post("/api/groups/{id}/expenses", groupId), """
                {"description": "Compra", "amount": 20, "paidById": %d, "splitType": "EQUAL",
                 "participants": [{"userId": %d}, {"userId": %d}]}
                """, ana.id(), ana.id(), bea.id()).andExpect(status().isCreated());

        deleteAs(ana, "/api/groups/{id}/members/{userId}", groupId, bea.id())
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value("No se puede eliminar a un miembro con balance distinto de 0"));
    }

    @Test
    void invalidGroupDataIsABadRequest() throws Exception {
        TestUser ana = registerUser("Ana");

        perform(ana, post("/api/groups"), """
                {"name": " ", "currency": "euros"}
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.currency").exists());
    }
}
