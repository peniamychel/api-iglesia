package com.mcmm.security.filters;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mcmm.model.dao.UsuarioDao;
import com.mcmm.model.entity.Cargo;
import com.mcmm.model.entity.Iglesia;
import com.mcmm.model.entity.RolCargo;
import com.mcmm.model.entity.Privilegio;
import com.mcmm.model.entity.Usuario;
import com.mcmm.security.jwt.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private JwtUtils jwtUtils;
    private UsuarioDao usuarioDao;
    private com.mcmm.service.IBitacora bitacoraService;

    public JwtAuthenticationFilter(JwtUtils jwtUtils, UsuarioDao usuarioDao, com.mcmm.service.IBitacora bitacoraService) {
        this.jwtUtils = jwtUtils;
        this.usuarioDao = usuarioDao;
        this.bitacoraService = bitacoraService;
    }
    @Override
    public Authentication attemptAuthentication(@NonNull HttpServletRequest request,
                                                @NonNull HttpServletResponse response) throws AuthenticationException {
        Usuario usuario = null;
        String username = "";
        String password = "";
        try{
            usuario = new ObjectMapper().readValue(request.getInputStream(), Usuario.class);
            username = usuario.getUsername();
            password = usuario.getPassword();

        } catch (StreamReadException e) {
            throw new RuntimeException(e);
        } catch (DatabindException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                new UsernamePasswordAuthenticationToken(username, password);

        return getAuthenticationManager().authenticate(usernamePasswordAuthenticationToken);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authResult) throws IOException, ServletException {
        User user = (User) authResult.getPrincipal();
        Usuario usuario = usuarioDao.findByUsername(user.getUsername()).orElse(null);

        if (usuario != null && usuario.getMiembro() != null) {
            List<Cargo> activeCargos = new ArrayList<>();
            if (usuario.getMiembro().getCargos() != null) {
                Date now = new Date();
                for (Cargo c : usuario.getMiembro().getCargos()) {
                    if (Boolean.TRUE.equals(c.getEstado()) &&
                            (c.getFechaFin() == null || c.getFechaFin().after(now))) {
                        activeCargos.add(c);
                    }
                }
            }

            Map<Long, List<Cargo>> cargosByChurch = new HashMap<>();
            for (Cargo c : activeCargos) {
                if (c.getIglesia() != null) {
                    cargosByChurch.computeIfAbsent(c.getIglesia().getId(), k -> new ArrayList<>()).add(c);
                }
            }

            if (cargosByChurch.size() > 1) {
                String preAuthToken = jwtUtils.gerarPreAuthToken(user.getUsername());

                List<Map<String, Object>> iglesiasInfo = new ArrayList<>();
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

                Map<String, Object> httpResponse = new HashMap<>();
                httpResponse.put("success", true);
                httpResponse.put("requiresSelection", true);
                httpResponse.put("preAuthToken", preAuthToken);
                httpResponse.put("username", user.getUsername());
                httpResponse.put("iglesias", iglesiasInfo);

                response.setStatus(HttpStatus.OK.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().write(new ObjectMapper().writeValueAsString(httpResponse));
                response.getWriter().flush();
                bitacoraService.registrar(usuario.getId(), usuario.getUsername(), "Acceso", "AUTENTICACION", "Pre-autenticación exitosa (requiere selección de iglesia/cargo)", getClientIp(request));
                return;
            } else if (cargosByChurch.size() == 1) {
                Map.Entry<Long, List<Cargo>> entry = cargosByChurch.entrySet().iterator().next();
                Long iglesiaId = entry.getKey();
                List<Cargo> churchCargos = entry.getValue();
                Iglesia iglesia = churchCargos.get(0).getIglesia();

                List<GrantedAuthority> authorities = new ArrayList<>(user.getAuthorities());
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
                        user.getUsername(),
                        uniqueAuthorities,
                        iglesiaId,
                        churchCargos.get(0).getId(),
                        iglesia.getNombre(),
                        churchCargos.get(0).getRolCargo() != null ? churchCargos.get(0).getRolCargo().getNombre() : null
                );
                String refreshToken = jwtUtils.gerarRefreshToken(user.getUsername());
                response.addHeader("Authorization", "Bearer " + token);

                Map<String, Object> httpResponse = new HashMap<>();
                httpResponse.put("success", true);
                httpResponse.put("token", token);
                httpResponse.put("refreshToken", refreshToken);
                httpResponse.put("message", "Autenticaion Exitosa");
                httpResponse.put("username", user.getUsername());
                List<Map<String, String>> uniqueRolesAsObjects = uniqueAuthorities.stream()
                        .map(a -> { Map<String, String> m = new HashMap<>(); m.put("authority", a.getAuthority()); return m; })
                        .collect(Collectors.toList());
                httpResponse.put("roles", uniqueRolesAsObjects);

                response.setStatus(HttpStatus.OK.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().write(new ObjectMapper().writeValueAsString(httpResponse));
                response.getWriter().flush();

                bitacoraService.registrar(usuario.getId(), usuario.getUsername(), "Acceso", "AUTENTICACION", "Inicio de sesión exitoso (Iglesia: " + iglesia.getNombre() + ")", getClientIp(request));

                super.successfulAuthentication(request, response, chain, authResult);
                return;
            }
        }

        // Caso especial: super-administrador sin miembro asignado
        // Las authorities ya fueron cargadas por UserDetailsServiceImpl con ROLE_ADMIN + todos los privilegios
        String token = jwtUtils.gerarAccessToken(user.getUsername(), user.getAuthorities());
        String refreshToken = jwtUtils.gerarRefreshToken(user.getUsername());
        response.addHeader("Authorization", "Bearer " + token);

        // El frontend espera roles como lista de objetos {authority: string}
        List<Map<String, String>> rolesAsObjects = user.getAuthorities().stream()
                .map(a -> { Map<String, String> m = new HashMap<>(); m.put("authority", a.getAuthority()); return m; })
                .collect(Collectors.toList());

        Map<String, Object> httpResponse = new HashMap<>();
        httpResponse.put("success", true);
        httpResponse.put("token", token);
        httpResponse.put("refreshToken", refreshToken);
        httpResponse.put("message", "Autenticacion Exitosa");
        httpResponse.put("username", user.getUsername());
        httpResponse.put("roles", rolesAsObjects);

        response.setStatus(HttpStatus.OK.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(new ObjectMapper().writeValueAsString(httpResponse));
        response.getWriter().flush();

        if (usuario != null) {
            bitacoraService.registrar(usuario.getId(), usuario.getUsername(), "Acceso", "AUTENTICACION", "Inicio de sesión exitoso (Super Admin)", getClientIp(request));
        }

        super.successfulAuthentication(request, response, chain, authResult);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request,
                                              HttpServletResponse response,
                                              AuthenticationException failed) throws IOException, ServletException {
        Map<String, Object> httpResponse = new HashMap<>();
        httpResponse.put("success", false);
        httpResponse.put("message", "Error de autenticación: Usuario o contraseña incorrectos");
        httpResponse.put("error", failed.getClass().getSimpleName());

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(new ObjectMapper().writeValueAsString(httpResponse));
        response.getWriter().flush();

        // Intentar registrar el fallo en bitácora
        bitacoraService.registrar(null, "Desconocido", "Advertencia", "AUTENTICACION", "Intento fallido de inicio de sesión (Credenciales incorrectas)", getClientIp(request));
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isEmpty()) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }
}
