package com.sharedexpenses.shared.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class ClientIpResolverTest {

    @Test
    void usesTheTrustedHeaderWhenConfigured() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("172.70.1.1");
        request.addHeader("CF-Connecting-IP", "79.117.174.154");

        assertThat(new ClientIpResolver("CF-Connecting-IP").resolve(request)).isEqualTo("79.117.174.154");
    }

    @Test
    void fallsBackToTheConnectionAddress() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.5");

        assertThat(new ClientIpResolver("CF-Connecting-IP").resolve(request)).isEqualTo("10.0.0.5");
        assertThat(new ClientIpResolver("").resolve(request)).isEqualTo("10.0.0.5");
    }

    @Test
    void ignoresTheHeaderWhenNoneIsTrusted() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.5");
        request.addHeader("CF-Connecting-IP", "1.2.3.4");

        assertThat(new ClientIpResolver(null).resolve(request)).isEqualTo("10.0.0.5");
    }
}
