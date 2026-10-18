package com.sharedexpenses.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "app.client-ip-header=CF-Connecting-IP",
        "app.rate-limit.login-per-client.max-requests=2"})
@AutoConfigureMockMvc
class RateLimitBehindProxyApiTest {

    @Autowired
    MockMvc mvc;

    @Test
    void countsTheRealClientEvenWhenEachRequestArrivesThroughADifferentProxy() throws Exception {
        mvc.perform(login("172.70.0.1", "79.117.174.154")).andExpect(status().isUnauthorized());
        mvc.perform(login("172.70.0.2", "79.117.174.154")).andExpect(status().isUnauthorized());

        mvc.perform(login("172.70.0.3", "79.117.174.154")).andExpect(status().isTooManyRequests());
        mvc.perform(login("172.70.0.3", "88.1.2.3")).andExpect(status().isUnauthorized());
    }

    private static MockHttpServletRequestBuilder login(String proxyIp, String clientIp) {
        return post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .header("CF-Connecting-IP", clientIp)
                .content("""
                        {"email": "nadie@test.com", "password": "password123"}
                        """)
                .with(request -> {
                    request.setRemoteAddr(proxyIp);
                    return request;
                });
    }
}
