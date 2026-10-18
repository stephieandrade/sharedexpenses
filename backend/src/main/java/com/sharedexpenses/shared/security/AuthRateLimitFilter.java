package com.sharedexpenses.shared.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class AuthRateLimitFilter extends OncePerRequestFilter {

    static final String REGISTER_PATH = "/api/auth/register";
    static final String LOGIN_PATH = "/api/auth/login";

    private static final Logger log = LoggerFactory.getLogger(AuthRateLimitFilter.class);

    private record Rule(FixedWindowRateLimiter limiter, Function<HttpServletRequest, String> key) {
    }

    private final Map<String, List<Rule>> rulesByPath;
    private final ProblemDetailWriter problemWriter;
    private final ClientIpResolver clientIp;

    public AuthRateLimitFilter(RateLimitProperties properties, Clock clock, ProblemDetailWriter problemWriter,
                               ClientIpResolver clientIp) {
        this.problemWriter = problemWriter;
        this.clientIp = clientIp;
        Function<HttpServletRequest, String> perClient = clientIp::resolve;
        Function<HttpServletRequest, String> total = request -> "all";
        this.rulesByPath = Map.of(
                REGISTER_PATH, List.of(
                        new Rule(limiter(properties.registerPerClient(), clock), perClient),
                        new Rule(limiter(properties.registerTotal(), clock), total)),
                LOGIN_PATH, List.of(new Rule(limiter(properties.loginPerClient(), clock), perClient)));
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !HttpMethod.POST.matches(request.getMethod()) || !rulesByPath.containsKey(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        for (Rule rule : rulesByPath.get(request.getRequestURI())) {
            Optional<Duration> retryAfter = rule.limiter().tryAcquire(rule.key().apply(request));
            if (retryAfter.isPresent()) {
                reject(request, response, retryAfter.get());
                return;
            }
        }
        chain.doFilter(request, response);
    }

    private void reject(HttpServletRequest request, HttpServletResponse response, Duration retryAfter)
            throws IOException {
        long seconds = Math.max(1, retryAfter.toSeconds());
        long minutes = Math.max(1, (seconds + 59) / 60);
        log.warn("Rate limit exceeded on {} from {}", request.getRequestURI(), clientIp.resolve(request));
        response.setHeader(HttpHeaders.RETRY_AFTER, String.valueOf(seconds));
        problemWriter.write(request, response, HttpStatus.TOO_MANY_REQUESTS,
                "Demasiados intentos. Vuelve a intentarlo en " + minutes + (minutes == 1 ? " minuto" : " minutos"));
    }

    private static FixedWindowRateLimiter limiter(RateLimitProperties.Limit limit, Clock clock) {
        return new FixedWindowRateLimiter(limit.maxRequests(), limit.window(), clock);
    }
}
