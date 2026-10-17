package com.sharedexpenses.shared.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Tag(name = "Sistema")
@SecurityRequirements
public class HealthController {

    @GetMapping("/api/health")
    @Operation(summary = "Comprueba que el servicio está en marcha")
    public Map<String, String> health() {
        return Map.of("status", "UP");
    }
}
