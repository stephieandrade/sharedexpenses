package com.sharedexpenses.shared.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.client.RestClient;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.Clock;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableConfigurationProperties({RateLimitProperties.class, CaptchaProperties.class})
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);
    private static final String[] PUBLIC_DOCS = {"/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**"};

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, ProblemDetailSecurityHandler problems,
                                            RateLimitProperties rateLimits, CaptchaProperties captcha,
                                            CaptchaVerifier captchaVerifier, Clock clock,
                                            ProblemDetailWriter problemWriter) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(new AuthRateLimitFilter(rateLimits, clock, problemWriter),
                        BearerTokenAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/health").permitAll()
                        .requestMatchers(PUBLIC_DOCS).permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth -> oauth
                        .jwt(Customizer.withDefaults())
                        .authenticationEntryPoint(problems)
                        .accessDeniedHandler(problems))
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(problems)
                        .accessDeniedHandler(problems));

        if (captcha.enabled()) {
            http.addFilterAfter(new RegistrationCaptchaFilter(captchaVerifier, problemWriter),
                    AuthRateLimitFilter.class);
        } else {
            log.warn("TURNSTILE_SECRET is not set: the registration captcha is disabled");
        }
        return http.build();
    }

    @Bean
    CaptchaVerifier captchaVerifier(RestClient.Builder restClientBuilder, CaptchaProperties captcha) {
        return new TurnstileCaptchaVerifier(restClientBuilder, captcha);
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource(@Value("${app.frontend-url}") String frontendUrls) {
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOrigins(Arrays.stream(frontendUrls.split(","))
                .map(String::strip)
                .filter(origin -> !origin.isEmpty())
                .toList());
        cors.setAllowedMethods(List.of("GET", "POST", "DELETE", "OPTIONS"));
        cors.setAllowedHeaders(List.of("Authorization", "Content-Type", RegistrationCaptchaFilter.CAPTCHA_HEADER));
        cors.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", cors);
        return source;
    }
}
