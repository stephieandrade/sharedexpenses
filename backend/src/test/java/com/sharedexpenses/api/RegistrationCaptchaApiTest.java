package com.sharedexpenses.api;

import com.sharedexpenses.shared.security.CaptchaVerifier;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "app.captcha.secret=test-secret",
        "app.rate-limit.register-per-client.max-requests=100000",
        "app.rate-limit.register-total.max-requests=100000"})
@AutoConfigureMockMvc
class RegistrationCaptchaApiTest {

    private static final String CAPTCHA_HEADER = "X-Captcha-Token";

    @Autowired
    MockMvc mvc;

    @MockitoBean
    CaptchaVerifier captchaVerifier;

    @Test
    void registrationWithAValidCaptchaSucceeds() throws Exception {
        when(captchaVerifier.verify("valid-token", "10.1.1.1")).thenReturn(true);

        mvc.perform(register().header(CAPTCHA_HEADER, "valid-token")).andExpect(status().isCreated());
    }

    @Test
    void registrationWithoutCaptchaIsRejectedWithoutCallingCloudflare() throws Exception {
        mvc.perform(register())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Falta la verificación anti-bots. Completa la casilla de verificación"));
        verify(captchaVerifier, never()).verify(any(), any());
    }

    @Test
    void registrationWithAnInvalidCaptchaIsRejected() throws Exception {
        when(captchaVerifier.verify("bot-token", "10.1.1.1")).thenReturn(false);

        mvc.perform(register().header(CAPTCHA_HEADER, "bot-token"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("La verificación anti-bots no es válida. Vuelve a intentarlo"));
    }

    @Test
    void registrationFailsSafelyWhenCloudflareIsUnavailable() throws Exception {
        when(captchaVerifier.verify(any(), any())).thenThrow(new IllegalStateException("timeout"));

        mvc.perform(register().header(CAPTCHA_HEADER, "any-token")).andExpect(status().isServiceUnavailable());
    }

    @Test
    void loginDoesNotRequireACaptcha() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("""
                        {"email": "nadie@test.com", "password": "password123"}
                        """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void corsAllowsTheCaptchaHeader() throws Exception {
        mvc.perform(options("/api/auth/register")
                        .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "content-type, x-captcha-token"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, "content-type, x-captcha-token"));
    }

    private static MockHttpServletRequestBuilder register() {
        return post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"name": "Persona", "email": "persona-%s@test.com", "password": "password123"}
                        """.formatted(UUID.randomUUID()))
                .with(request -> {
                    request.setRemoteAddr("10.1.1.1");
                    return request;
                });
    }
}
