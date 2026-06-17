package com.mcmm.controller;

import com.mcmm.model.dao.UsuarioDao;
import com.mcmm.model.dto.auth.RefreshTokenRequest;
import com.mcmm.model.entity.Cargo;
import com.mcmm.model.entity.Iglesia;
import com.mcmm.model.entity.Privilegio;
import com.mcmm.model.entity.RolCargo;
import com.mcmm.model.entity.Usuario;
import com.mcmm.security.jwt.JwtUtils;
import com.mcmm.service.impl.UserDetailsServiceImpl;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private UsuarioDao usuarioDao;

    @Data
    public static class SelectCargoRequest {
        private String preAuthToken;
        private Long iglesiaId;
    }

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

    @PostMapping("/select-cargo")
    public ResponseEntity<?> selectCargo(@RequestBody SelectCargoRequest request) {
        String preAuthToken = request.getPreAuthToken();

        if (!jwtUtils.isPreAuthTokenValid(preAuthToken)) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Token de pre-autenticacion invalido o expirado");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        String username = jwtUtils.getUsernameFronToken(preAuthToken);
        Usuario usuario = usuarioDao.findByUsername(username).orElse(null);

        if (usuario == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Usuario no encontrado");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        List<Cargo> activeCargos = new ArrayList<>();
        if (usuario.getMiembro() != null && usuario.getMiembro().getCargos() != null) {
            Date now = new Date();
            for (Cargo c : usuario.getMiembro().getCargos()) {
                if (Boolean.TRUE.equals(c.getEstado()) &&
                        (c.getFechaFin() == null || c.getFechaFin().after(now))) {
                    activeCargos.add(c);
                }
            }
        }

        List<Cargo> churchCargos = new ArrayList<>();
        for (Cargo c : activeCargos) {
            if (c.getIglesia() != null && c.getIglesia().getId().equals(request.getIglesiaId())) {
                churchCargos.add(c);
            }
        }

        if (churchCargos.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "El usuario no tiene cargos activos en la iglesia seleccionada");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        Iglesia iglesia = churchCargos.get(0).getIglesia();
        List<GrantedAuthority> authorities = new ArrayList<>();

        try {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            authorities.addAll(userDetails.getAuthorities());
        } catch (Exception e) {
            // Ignorar o registrar si no se puede cargar las authorities base
        }

        for (Cargo c : churchCargos) {
            RolCargo rc = c.getRolCargo();
            if (rc != null) {
                if (rc.getNombreRol() != null) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + rc.getNombreRol()));
                }
                if (rc.getPrivilegios() != null) {
                    for (Privilegio p : rc.getPrivilegios()) {
                        if (p.getNombre() != null) {
                            authorities.add(new SimpleGrantedAuthority(p.getNombre()));
                        }
                    }
                }
            }
        }

        List<GrantedAuthority> uniqueAuthorities = authorities.stream().distinct().collect(Collectors.toList());

        String token = jwtUtils.gerarAccessToken(
                username,
                uniqueAuthorities,
                request.getIglesiaId(),
                churchCargos.get(0).getId(),
                iglesia.getNombre(),
                churchCargos.get(0).getRolCargo() != null ? churchCargos.get(0).getRolCargo().getNombre() : null
        );
        String refreshToken = jwtUtils.gerarRefreshToken(username);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("token", token);
        response.put("refreshToken", refreshToken);
        response.put("username", username);
        response.put("roles", uniqueAuthorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()));
        response.put("iglesias", getUserIglesias(usuario));

        return ResponseEntity.ok(response);
    }

    @PostMapping("/switch-church")
    public ResponseEntity<?> switchChurch(@RequestBody Map<String, Long> request) {
        Long iglesiaId = request.get("iglesiaId");
        org.springframework.security.core.Authentication authentication = 
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Usuario no autenticado");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        String username = authentication.getName();
        Usuario usuario = usuarioDao.findByUsername(username).orElse(null);

        if (usuario == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Usuario no encontrado");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }

        List<Cargo> activeCargos = new ArrayList<>();
        if (usuario.getMiembro() != null && usuario.getMiembro().getCargos() != null) {
            Date now = new Date();
            for (Cargo c : usuario.getMiembro().getCargos()) {
                if (Boolean.TRUE.equals(c.getEstado()) &&
                        (c.getFechaFin() == null || c.getFechaFin().after(now))) {
                    activeCargos.add(c);
                }
            }
        }

        List<Cargo> churchCargos = new ArrayList<>();
        for (Cargo c : activeCargos) {
            if (c.getIglesia() != null && c.getIglesia().getId().equals(iglesiaId)) {
                churchCargos.add(c);
            }
        }

        if (churchCargos.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "El usuario no tiene cargos activos en la iglesia seleccionada");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        Iglesia iglesia = churchCargos.get(0).getIglesia();
        List<GrantedAuthority> authorities = new ArrayList<>();

        try {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            authorities.addAll(userDetails.getAuthorities());
        } catch (Exception e) {
            // Ignorar
        }

        for (Cargo c : churchCargos) {
            RolCargo rc = c.getRolCargo();
            if (rc != null) {
                if (rc.getNombreRol() != null) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + rc.getNombreRol()));
                }
                if (rc.getPrivilegios() != null) {
                    for (Privilegio p : rc.getPrivilegios()) {
                        if (p.getNombre() != null) {
                            authorities.add(new SimpleGrantedAuthority(p.getNombre()));
                        }
                    }
                }
            }
        }

        List<GrantedAuthority> uniqueAuthorities = authorities.stream().distinct().collect(Collectors.toList());

        String token = jwtUtils.gerarAccessToken(
                username,
                uniqueAuthorities,
                iglesiaId,
                churchCargos.get(0).getId(),
                iglesia.getNombre(),
                churchCargos.get(0).getRolCargo() != null ? churchCargos.get(0).getRolCargo().getNombre() : null
        );
        String refreshToken = jwtUtils.gerarRefreshToken(username);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("token", token);
        response.put("refreshToken", refreshToken);
        response.put("username", username);
        response.put("roles", uniqueAuthorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()));
        response.put("iglesias", getUserIglesias(usuario));

        return ResponseEntity.ok(response);
    }

    private List<Map<String, Object>> getUserIglesias(Usuario usuario) {
        List<Map<String, Object>> iglesiasInfo = new ArrayList<>();
        if (usuario != null && usuario.getMiembro() != null && usuario.getMiembro().getCargos() != null) {
            Date now = new Date();
            List<Cargo> activeCargos = new ArrayList<>();
            for (Cargo c : usuario.getMiembro().getCargos()) {
                if (Boolean.TRUE.equals(c.getEstado()) &&
                        (c.getFechaFin() == null || c.getFechaFin().after(now))) {
                    activeCargos.add(c);
                }
            }

            Map<Long, List<Cargo>> cargosByChurch = new HashMap<>();
            for (Cargo c : activeCargos) {
                if (c.getIglesia() != null) {
                    cargosByChurch.computeIfAbsent(c.getIglesia().getId(), k -> new ArrayList<>()).add(c);
                }
            }

            for (Map.Entry<Long, List<Cargo>> entry : cargosByChurch.entrySet()) {
                Iglesia iglesia = entry.getValue().get(0).getIglesia();
                Map<String, Object> iglesiaMap = new HashMap<>();
                iglesiaMap.put("iglesiaId", entry.getKey());
                iglesiaMap.put("iglesiaNombre", iglesia.getNombre());
                List<String> activeCargosNames = entry.getValue().stream()
                        .filter(c -> c.getRolCargo() != null)
                        .map(c -> c.getRolCargo().getNombre())
                        .distinct()
                        .collect(Collectors.toList());
                iglesiaMap.put("cargos", activeCargosNames);
                iglesiasInfo.add(iglesiaMap);
            }
        }
        return iglesiasInfo;
    }
}
