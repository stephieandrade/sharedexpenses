package com.sharedexpenses.shared.security;

import jakarta.servlet.http.HttpServletRequest;

public class ClientIpResolver {

    private final String trustedHeader;

    public ClientIpResolver(String trustedHeader) {
        this.trustedHeader = trustedHeader == null || trustedHeader.isBlank() ? null : trustedHeader.strip();
    }

    public String resolve(HttpServletRequest request) {
        if (trustedHeader != null) {
            String value = request.getHeader(trustedHeader);
            if (value != null && !value.isBlank()) {
                return value.split(",")[0].strip();
            }
        }
        return request.getRemoteAddr();
    }
}
