package com.subscriptiq.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.subscriptiq.common.response.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Lightweight in-memory rate limiting filter for sensitive endpoints (e.g. auth).
 * Provides denial-of-service and brute-force protection without external dependencies.
 */
@Slf4j
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    @Value("${application.security.rate-limit.enabled:true}")
    private boolean enabled;

    @Value("${application.security.rate-limit.requests-per-minute:120}")
    private int maxRequestsPerMinute;

    private final Map<String, RequestCounter> requestCounts = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    public RateLimitFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void reset() {
        requestCounts.clear();
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        if (!enabled || !isRateLimitedEndpoint(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = getClientIp(request);
        long currentWindow = System.currentTimeMillis() / 60000;
        String key = clientIp + ":" + currentWindow;

        RequestCounter counter = requestCounts.compute(key, (k, v) -> {
            if (v == null || v.window != currentWindow) {
                return new RequestCounter(currentWindow, new AtomicInteger(1));
            }
            v.count.incrementAndGet();
            return v;
        });

        // Cleanup old windows occasionally
        if (requestCounts.size() > 5000) {
            requestCounts.entrySet().removeIf(e -> e.getValue().window < currentWindow - 2);
        }

        if (counter.count.get() > maxRequestsPerMinute) {
            log.warn("Rate limit exceeded for IP {} on {}", clientIp, request.getRequestURI());
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            ErrorResponse error = ErrorResponse.builder()
                    .status(HttpStatus.TOO_MANY_REQUESTS.value())
                    .error("Too Many Requests")
                    .message("Rate limit exceeded. Please try again in a few moments.")
                    .path(request.getRequestURI())
                    .timestamp(LocalDateTime.now())
                    .build();

            response.getWriter().write(objectMapper.writeValueAsString(error));
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isRateLimitedEndpoint(String uri) {
        return uri != null && (
                uri.startsWith("/api/auth/login") ||
                uri.startsWith("/api/auth/forgot-password") ||
                uri.startsWith("/api/auth/reset-password")
        );
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr() != null ? request.getRemoteAddr() : "unknown";
    }

    private static class RequestCounter {
        final long window;
        final AtomicInteger count;

        RequestCounter(long window, AtomicInteger count) {
            this.window = window;
            this.count = count;
        }
    }
}
