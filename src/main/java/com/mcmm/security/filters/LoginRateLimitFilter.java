package com.mcmm.security.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class LoginRateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_ATTEMPTS = 5;
    private static final long WINDOW_MS = 60_000;
    private static final long CLEANUP_INTERVAL_MS = 5 * 60_000;

    private final Map<String, RateBucket> attempts = new ConcurrentHashMap<>();
    private volatile long lastCleanup = System.currentTimeMillis();

    /**
     * Solo cuando la app corre detras de un proxy/balanceador de confianza que
     * ya sanea la cabecera se debe confiar en X-Forwarded-For. Por defecto NO,
     * para que un atacante no pueda evadir el limite rotando la cabecera.
     */
    @Value("${security.rate-limit.trust-forwarded-header:false}")
    private boolean trustForwardedHeader;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        if (!"/login".equals(path) || !"POST".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        long now = System.currentTimeMillis();
        cleanupIfNeeded(now);

        String clientIp = getClientIp(request);
        RateBucket bucket = attempts.computeIfAbsent(clientIp, k -> new RateBucket());

        synchronized (bucket) {
            if (now - bucket.windowStart > WINDOW_MS) {
                bucket.windowStart = now;
                bucket.count.set(0);
            }

            if (bucket.count.incrementAndGet() > MAX_ATTEMPTS) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().write(
                    new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(Map.of(
                        "success", false,
                        "message", "Demasiados intentos de inicio de sesion. Intente nuevamente en un minuto."
                    ))
                );
                response.getWriter().flush();
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        if (trustForwardedHeader) {
            String xfHeader = request.getHeader("X-Forwarded-For");
            if (xfHeader != null && !xfHeader.isEmpty()) {
                return xfHeader.split(",")[0].trim();
            }
        }
        return request.getRemoteAddr();
    }

    /**
     * Purga periodica de buckets cuya ventana ya expiro, para evitar que el mapa
     * crezca sin limite (fuga de memoria / vector de DoS por IPs distintas).
     */
    private void cleanupIfNeeded(long now) {
        if (now - lastCleanup < CLEANUP_INTERVAL_MS) {
            return;
        }
        lastCleanup = now;
        attempts.entrySet().removeIf(e -> now - e.getValue().windowStart > WINDOW_MS);
    }

    private static class RateBucket {
        long windowStart = System.currentTimeMillis();
        AtomicInteger count = new AtomicInteger(0);
    }
}
