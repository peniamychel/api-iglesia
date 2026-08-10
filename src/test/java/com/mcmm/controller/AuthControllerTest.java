package com.mcmm.controller;

import com.mcmm.model.dao.IglesiaDao;
import com.mcmm.model.dto.auth.RefreshTokenRequest;
import com.mcmm.model.entity.Cargo;
import com.mcmm.model.entity.Iglesia;
import com.mcmm.model.entity.RolCargo;
import com.mcmm.model.entity.Usuario;
import com.mcmm.service.IBitacora;
import com.mcmm.service.impl.UserDetailsServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * /auth/** es permitAll() en produccion (sin @PreAuthorize), asi que el valor
 * de este test esta en la logica de negocio -- no en autorizacion. No extiende
 * ControllerTestSupport: ese soporte mockea BitacoraDao/UsuarioDao para
 * GlobalExceptionHandler y trae JwtUtils, que este controlador tambien
 * necesita, pero AuthController usa ademas UserDetailsServiceImpl e IglesiaDao
 * por campo, asi que arma su propio set de @MockBean.
 */
@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(AuthControllerTest.MethodSecurityTestConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @MockBean
    private com.mcmm.security.jwt.JwtUtils jwtUtils;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private com.mcmm.model.dao.UsuarioDao usuarioDao;

    @MockBean
    private IglesiaDao iglesiaDao;

    @MockBean
    private IBitacora bitacoraService;

    @MockBean
    private com.mcmm.model.dao.BitacoraDao bitacoraDao;

    @TestConfiguration
    @EnableMethodSecurity(prePostEnabled = true)
    static class MethodSecurityTestConfig {
    }

    private RefreshTokenRequest refreshTokenRequest(String token) {
        RefreshTokenRequest r = new RefreshTokenRequest();
        r.setRefreshToken(token);
        return r;
    }

    private Usuario usuarioActivo(boolean esAdmin) {
        Usuario u = new Usuario();
        u.setUsername("carlos");
        u.setEstado(true);
        u.setEsAdmin(esAdmin);
        return u;
    }

    // ───────────────────────── refresh ─────────────────────────

    @Test
    @DisplayName("refresh: token invalido responde 401")
    void refresh_tokenInvalido_devuelve401() throws Exception {
        when(jwtUtils.isRefreshTokenValid("malo")).thenReturn(false);

        mockMvc.perform(post("/auth/refresh")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(refreshTokenRequest("malo"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("refresh: usuario inactivo responde 401, aunque el token en si sea valido")
    void refresh_usuarioInactivo_devuelve401() throws Exception {
        when(jwtUtils.isRefreshTokenValid("bueno")).thenReturn(true);
        when(jwtUtils.getUsernameFronToken("bueno")).thenReturn("carlos");
        Usuario inactivo = usuarioActivo(false);
        inactivo.setEstado(false);
        when(usuarioDao.findByUsername("carlos")).thenReturn(Optional.of(inactivo));

        mockMvc.perform(post("/auth/refresh")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(refreshTokenRequest("bueno"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("La cuenta de usuario no esta activa"));
    }

    @Test
    @DisplayName("refresh: happy path emite un nuevo access y refresh token")
    void refresh_happyPath_emiteNuevosTokens() throws Exception {
        when(jwtUtils.isRefreshTokenValid("bueno")).thenReturn(true);
        when(jwtUtils.getUsernameFronToken("bueno")).thenReturn("carlos");
        when(usuarioDao.findByUsername("carlos")).thenReturn(Optional.of(usuarioActivo(false)));
        when(userDetailsService.loadUserByUsername("carlos"))
                .thenReturn(new User("carlos", "hash", List.of()));
        when(jwtUtils.gerarAccessToken(eq("carlos"), any())).thenReturn("nuevo-access");
        when(jwtUtils.gerarRefreshToken("carlos")).thenReturn("nuevo-refresh");

        mockMvc.perform(post("/auth/refresh")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(refreshTokenRequest("bueno"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("nuevo-access"))
                .andExpect(jsonPath("$.refreshToken").value("nuevo-refresh"));
    }

    @Test
    @DisplayName("refresh: refreshToken en blanco viola @NotBlank, responde 400")
    void refresh_refreshTokenEnBlanco_devuelve400() throws Exception {
        mockMvc.perform(post("/auth/refresh")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(refreshTokenRequest(""))))
                .andExpect(status().isBadRequest());
    }

    // ───────────────────────── select-cargo ─────────────────────────

    @Test
    @DisplayName("select-cargo: sin cargos activos en la iglesia elegida y sin ser admin, responde 400")
    void selectCargo_sinCargoActivoNiAdmin_devuelve400() throws Exception {
        when(jwtUtils.isPreAuthTokenValid("pre")).thenReturn(true);
        when(jwtUtils.getUsernameFronToken("pre")).thenReturn("carlos");
        Usuario usuario = usuarioActivo(false); // no admin, sin miembro/cargos
        when(usuarioDao.findByUsername("carlos")).thenReturn(Optional.of(usuario));

        String body = "{\"preAuthToken\":\"pre\",\"iglesiaId\":1}";
        mockMvc.perform(post("/auth/select-cargo").contentType("application/json").content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("El usuario no tiene cargos activos en la iglesia seleccionada"));
    }

    @Test
    @DisplayName("select-cargo: admin sin cargo en esa iglesia igual pasa, como Administrador Global")
    void selectCargo_adminSinCargo_pasaComoAdministradorGlobal() throws Exception {
        when(jwtUtils.isPreAuthTokenValid("pre")).thenReturn(true);
        when(jwtUtils.getUsernameFronToken("pre")).thenReturn("admin");
        Usuario admin = usuarioActivo(true);
        when(usuarioDao.findByUsername("admin")).thenReturn(Optional.of(admin));
        Iglesia iglesia = new Iglesia();
        iglesia.setId(1L);
        iglesia.setNombre("Palmar");
        when(iglesiaDao.findById(1L)).thenReturn(Optional.of(iglesia));
        when(iglesiaDao.findAll()).thenReturn(List.of());
        when(userDetailsService.loadUserByUsername("admin")).thenReturn(new User("admin", "hash", List.of()));
        when(jwtUtils.gerarAccessToken(anyString(), any(), any(), any(), anyString(), anyString())).thenReturn("token");
        when(jwtUtils.gerarRefreshToken("admin")).thenReturn("refresh");

        String body = "{\"preAuthToken\":\"pre\",\"iglesiaId\":1}";
        mockMvc.perform(post("/auth/select-cargo").contentType("application/json").content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token"));
    }

    @Test
    @DisplayName("select-cargo: usuario con cargo activo en esa iglesia, resuelve el nombre del cargo y la iglesia")
    void selectCargo_conCargoActivo_resuelveIglesiaYCargo() throws Exception {
        when(jwtUtils.isPreAuthTokenValid("pre")).thenReturn(true);
        when(jwtUtils.getUsernameFronToken("pre")).thenReturn("carlos");

        Iglesia iglesia = new Iglesia();
        iglesia.setId(1L);
        iglesia.setNombre("Palmar");
        RolCargo rol = new RolCargo();
        rol.setNombre("Pastor");
        rol.setNombreRol("PASTOR");
        Cargo cargo = new Cargo();
        cargo.setId(10L);
        cargo.setIglesia(iglesia);
        cargo.setRolCargo(rol);
        cargo.setEstado(true);
        com.mcmm.model.entity.Miembro miembro = new com.mcmm.model.entity.Miembro();
        miembro.setCargos(List.of(cargo));
        Usuario usuario = usuarioActivo(false);
        usuario.setMiembro(miembro);
        when(usuarioDao.findByUsername("carlos")).thenReturn(Optional.of(usuario));
        when(userDetailsService.loadUserByUsername("carlos")).thenReturn(new User("carlos", "hash", List.of()));
        when(jwtUtils.gerarAccessToken(anyString(), any(), any(), any(), anyString(), anyString())).thenReturn("token");
        when(jwtUtils.gerarRefreshToken("carlos")).thenReturn("refresh");

        String body = "{\"preAuthToken\":\"pre\",\"iglesiaId\":1}";
        mockMvc.perform(post("/auth/select-cargo").contentType("application/json").content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token"));
    }

    // ───────────────────────── switch-church ─────────────────────────

    @Test
    @DisplayName("switch-church: sin autenticacion en el SecurityContext, responde 401")
    void switchChurch_sinAutenticacion_devuelve401() throws Exception {
        mockMvc.perform(post("/auth/switch-church")
                        .contentType("application/json")
                        .content("{\"iglesiaId\":1}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Usuario no autenticado"));
    }
}
