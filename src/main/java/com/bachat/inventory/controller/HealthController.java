package com.bachat.inventory.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "Health", description = "Health check endpoints")
@RestController
public class HealthController {

    @Operation(
            summary = "Service health check",
            description = "Returns basic service status information. Useful for monitoring and verifying the backend is running."
    )
    @GetMapping("/api/v1/health")
    public Map<String, Object> health() {
        return Map.of(
                "status", "ok",
                "service", "inventory-backend"
        );
    }
}
