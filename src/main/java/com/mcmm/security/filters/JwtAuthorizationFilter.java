package com.mcmm.security.filters;

import com.mcmm.security.jwt.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthorizationFilter.class);

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            String tokenHeader = request.getHeader("Authorization");
            if (tokenHeader != null && tokenHeader.startsWith("Bearer ")) {
                String token = tokenHeader.substring(7);

                if (jwtUtils.isTokenValid(token)) {
                    String username = jwtUtils.getUsernameFronToken(token);
                    List<GrantedAuthority> authorities = jwtUtils.getAuthoritiesFromToken(token);

                    UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                            username, null, authorities);
                    
                    java.util.Map<String, Object> details = new java.util.HashMap<>();
                    details.put("iglesiaId", jwtUtils.getClaim(token, claims -> {
                        Object val = claims.get("iglesiaId");
                        return val instanceof Number ? ((Number) val).longValue() : null;
                    }));
                    details.put("cargoId", jwtUtils.getClaim(token, claims -> {
                        Object val = claims.get("cargoId");
                        return val instanceof Number ? ((Number) val).longValue() : null;
                    }));
                    details.put("iglesiaNombre", jwtUtils.getClaim(token, claims -> claims.get("iglesiaNombre", String.class)));
                    details.put("cargoNombre", jwtUtils.getClaim(token, claims -> claims.get("cargoNombre", String.class)));
                    authenticationToken.setDetails(details);

                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                }
            }
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.error("Error en autenticacion JWT", e);
            SecurityContextHolder.clearContext();
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Error de autenticacion");
        }
    }
}
