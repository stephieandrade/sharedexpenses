package com.sharedexpenses.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "app.rate-limit.register-per-client.max-requests=2",
        "app.rate-limit.register-total.max-requests=3",
        "app.rate-limit.login-per-client.max-requests=2"})
@AutoConfigureMockMvc
class RateLimitApiTest {

    @Autowired
    MockMvc mvc;

    @Test
    void registrationsAreLimitedPerClientAndForTheWholeApp() throws Exception {
        mvc.perform(register("10.0.0.1")).andExpect(status().isCreated());
        mvc.perform(register("10.0.0.1")).andExpect(status().isCreated());

        mvc.perform(register("10.0.0.1"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists(HttpHeaders.RETRY_AFTER))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").value("Demasiados intentos. Vuelve a intentarlo en 60 minutos"));

        mvc.perform(register("10.0.0.2")).andExpect(status().isCreated());
        mvc.perform(register("10.0.0.3")).andExpect(status().isTooManyRequests());
    }

    @Test
    void loginAttemptsAreLimitedPerClient() throws Exception {
        mvc.perform(login("10.0.1.1")).andExpect(status().isUnauthorized());
        mvc.perform(login("10.0.1.1")).andExpect(status().isUnauthorized());

        mvc.perform(login("10.0.1.1")).andExpect(status().isTooManyRequests());
        mvc.perform(login("10.0.1.2")).andExpect(status().isUnauthorized());
    }

    private static MockHttpServletRequestBuilder register(String clientIp) {
        return fromClient(post("/api/auth/register"), clientIp).content("""
                {"name": "Bot", "email": "bot-%s@test.com", "password": "password123"}
                """.formatted(UUID.randomUUID()));
    }

    private static MockHttpServletRequestBuilder login(String clientIp) {
        return fromClient(post("/api/auth/login"), clientIp).content("""
                {"email": "nadie@test.com", "password": "password123"}
                """);
    }

    private static MockHttpServletRequestBuilder fromClient(MockHttpServletRequestBuilder request, String clientIp) {
        return request.contentType(MediaType.APPLICATION_JSON).with(r -> {
            r.setRemoteAddr(clientIp);
            return r;
        });
    }
}
