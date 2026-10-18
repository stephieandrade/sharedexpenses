package com.sharedexpenses.shared.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class RegistrationCaptchaFilter extends OncePerRequestFilter {

    public static final String CAPTCHA_HEADER = "X-Captcha-Token";

    private static final Logger log = LoggerFactory.getLogger(RegistrationCaptchaFilter.class);

    private final CaptchaVerifier verifier;
    private final ProblemDetailWriter problemWriter;
    private final ClientIpResolver clientIp;

    public RegistrationCaptchaFilter(CaptchaVerifier verifier, ProblemDetailWriter problemWriter,
                                     ClientIpResolver clientIp) {
        this.verifier = verifier;
        this.problemWriter = problemWriter;
        this.clientIp = clientIp;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !HttpMethod.POST.matches(request.getMethod())
                || !AuthRateLimitFilter.REGISTER_PATH.equals(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String token = request.getHeader(CAPTCHA_HEADER);
        if (token == null || token.isBlank()) {
            problemWriter.write(request, response, HttpStatus.BAD_REQUEST,
                    "Falta la verificación anti-bots. Completa la casilla de verificación");
            return;
        }

        boolean human;
        try {
            human = verifier.verify(token, clientIp.resolve(request));
        } catch (RuntimeException e) {
            log.warn("Captcha verification unavailable: {}", e.getMessage());
            problemWriter.write(request, response, HttpStatus.SERVICE_UNAVAILABLE,
                    "No se ha podido comprobar la verificación anti-bots. Inténtalo de nuevo en unos segundos");
            return;
        }
        if (!human) {
            problemWriter.write(request, response, HttpStatus.BAD_REQUEST,
                    "La verificación anti-bots no es válida. Vuelve a intentarlo");
            return;
        }
        chain.doFilter(request, response);
    }
}
