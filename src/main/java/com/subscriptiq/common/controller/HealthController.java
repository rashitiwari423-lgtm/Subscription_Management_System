package com.subscriptiq.common.controller;

import com.subscriptiq.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "Health", description = "Service health check")
public class HealthController {

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Returns service status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> healthCheck() {
        Map<String, Object> healthData = new LinkedHashMap<>();
        healthData.put("status", "UP");
        healthData.put("service", "SubscriptIQ Platform");
        healthData.put("version", "1.0.0");
        return ResponseEntity.ok(ApiResponse.success("Service is healthy and operational", healthData));
    }
}
