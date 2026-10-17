package com.sharedexpenses.api;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SecurityApiTest extends ApiTestSupport {

    @Test
    void requestsWithoutTokenAreUnauthorized() throws Exception {
        mvc.perform(get("/api/groups"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.instance").value("/api/groups"));
    }

    @Test
    void requestsWithAnInvalidTokenAreUnauthorized() throws Exception {
        mvc.perform(get("/api/groups").header(HttpHeaders.AUTHORIZATION, "Bearer not-a-real-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void nonMembersCannotSeeOrChangeAGroup() throws Exception {
        TestUser owner = registerUser("Owner");
        TestUser outsider = registerUser("Outsider");
        Long groupId = createGroup(owner, "Privado");

        getAs(outsider, "/api/groups/{id}", groupId).andExpect(status().isForbidden());
        getAs(outsider, "/api/groups/{id}/expenses", groupId).andExpect(status().isForbidden());
        getAs(outsider, "/api/groups/{id}/balances", groupId).andExpect(status().isForbidden());
        getAs(outsider, "/api/groups/{id}/settlement", groupId).andExpect(status().isForbidden());
        perform(outsider, post("/api/groups/{id}/members", groupId), """
                {"email": "%s"}
                """, outsider.email()).andExpect(status().isForbidden());
        perform(outsider, post("/api/groups/{id}/expenses", groupId), """
                {"description": "Intruso", "amount": 10, "paidById": %d, "splitType": "EQUAL",
                 "participants": [{"userId": %d}]}
                """, outsider.id(), outsider.id())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.detail").value("No eres miembro de este grupo"));
        getAs(outsider, "/api/groups").andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void unknownGroupsAreNotFound() throws Exception {
        TestUser ana = registerUser("Ana");

        getAs(ana, "/api/groups/{id}", 999_999).andExpect(status().isNotFound());
    }

    @Test
    void corsAllowsOnlyTheFrontendOrigin() throws Exception {
        mvc.perform(options("/api/groups")
                        .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://localhost:5173"));

        mvc.perform(options("/api/groups")
                        .header(HttpHeaders.ORIGIN, "https://evil.example.com")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
                .andExpect(status().isForbidden());
    }

    @Test
    void healthCheckIsPublic() throws Exception {
        mvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    void apiDocumentationIsPublic() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("SharedExpenses API"));
    }
}
