package com.sharedexpenses.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "app.rate-limit.register-per-client.max-requests=100000",
        "app.rate-limit.register-total.max-requests=100000",
        "app.rate-limit.login-per-client.max-requests=100000"})
@AutoConfigureMockMvc
abstract class ApiTestSupport {

    protected static final String PASSWORD = "password123";

    @Autowired
    protected MockMvc mvc;

    @Autowired
    protected ObjectMapper objectMapper;

    protected record TestUser(Long id, String name, String email, String token) {
    }

    protected TestUser registerUser(String name) throws Exception {
        String email = name.toLowerCase() + "-" + UUID.randomUUID() + "@test.com";
        JsonNode body = read(mvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("""
                                {"name": "%s", "email": "%s", "password": "%s"}
                                """, name, email, PASSWORD)))
                .andExpect(status().isCreated()));
        return new TestUser(body.at("/user/id").asLong(), name, email, body.get("token").asText());
    }

    protected Long createGroup(TestUser creator, String name, TestUser... members) throws Exception {
        JsonNode group = read(perform(creator, post("/api/groups"), """
                {"name": "%s"}
                """, name).andExpect(status().isCreated()));
        Long groupId = group.get("id").asLong();
        for (TestUser member : members) {
            perform(creator, post("/api/groups/{id}/members", groupId), """
                    {"email": "%s"}
                    """, member.email()).andExpect(status().isCreated());
        }
        return groupId;
    }

    protected ResultActions perform(TestUser user, MockHttpServletRequestBuilder request, String body,
                                    Object... args) throws Exception {
        return mvc.perform(request
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + user.token())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(body, args)));
    }

    protected ResultActions getAs(TestUser user, String url, Object... vars) throws Exception {
        return mvc.perform(get(url, vars).header(HttpHeaders.AUTHORIZATION, "Bearer " + user.token()));
    }

    protected ResultActions deleteAs(TestUser user, String url, Object... vars) throws Exception {
        return mvc.perform(delete(url, vars).header(HttpHeaders.AUTHORIZATION, "Bearer " + user.token()));
    }

    protected JsonNode read(ResultActions result) throws Exception {
        return objectMapper.readTree(result.andReturn().getResponse().getContentAsString());
    }

    private static String json(String template, Object... args) {
        return args.length == 0 ? template : template.formatted(args);
    }
}
