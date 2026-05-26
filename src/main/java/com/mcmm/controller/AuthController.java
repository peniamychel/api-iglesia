package com.mcmm.controller;

import com.mcmm.model.dto.auth.RefreshTokenRequest;
import com.mcmm.security.jwt.JwtUtils;
import com.mcmm.service.impl.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        if (!jwtUtils.isRefreshTokenValid(refreshToken)) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Refresh token invalido o expirado");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        String username = jwtUtils.getUsernameFronToken(refreshToken);

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();

        String newAccessToken = jwtUtils.gerarAccessToken(username, authorities);
        String newRefreshToken = jwtUtils.gerarRefreshToken(username);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("token", newAccessToken);
        response.put("refreshToken", newRefreshToken);
        response.put("username", username);

        return ResponseEntity.ok(response);
    }
}
