package com.mcmm.service.impl;

import com.mcmm.model.dao.AccionDao;
import com.mcmm.model.dao.UsuarioDao;
import com.mcmm.model.entity.Accion;
import com.mcmm.model.entity.Cargo;
import com.mcmm.model.entity.Miembro;
import com.mcmm.model.entity.RolCargo;
import com.mcmm.model.entity.Servicio;
import com.mcmm.model.entity.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * loadUserByUsername() decide que puede hacer un usuario logueado: es el
 * punto de la aplicacion donde mas importa no equivocarse. Cubre en
 * particular los dos casos donde un cargo NO debe otorgar autoridad aunque
 * exista (estado=false, fecha_fin vencida) y el default-deny cuando
 * usuario.estado es NULL (no true, asi que enabled=false).
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserDetailsServiceImplTest {

    @Mock private UsuarioDao usuarioDao;
    @Mock private AccionDao accionDao;

    private UserDetailsServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UserDetailsServiceImpl();
        ReflectionTestUtils.setField(service, "usuarioDao", usuarioDao);
        ReflectionTestUtils.setField(service, "accionDao", accionDao);
    }

    private Usuario nuevoUsuario(String username, boolean estado, boolean esAdmin, Miembro miembro) {
        Usuario u = new Usuario();
        u.setUsername(username);
        u.setPassword("hash");
        u.setEstado(estado);
        u.setEsAdmin(esAdmin);
        u.setMiembro(miembro);
        return u;
    }

    private Accion nuevaAccion(String servicioCodigo, String codigo) {
        Servicio servicio = new Servicio();
        servicio.setCodigo(servicioCodigo);
        Accion a = new Accion();
        a.setServicio(servicio);
        a.setCodigo(codigo);
        return a;
    }

    private Cargo nuevoCargo(RolCargo rolCargo, boolean estado, Date fechaFin) {
        Cargo c = new Cargo();
        c.setRolCargo(rolCargo);
        c.setEstado(estado);
        c.setFechaFin(fechaFin);
        return c;
    }

    // ───────────────────────── usuario inexistente ─────────────────────────

    @Test
    @DisplayName("username inexistente: UsernameNotFoundException con mensaje claro")
    void loadUserByUsername_usuarioInexistente_lanzaUsernameNotFound() {
        when(usuarioDao.findByUsername("fantasma")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.loadUserByUsername("fantasma"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("fantasma");
    }

    // ───────────────────────── admin ─────────────────────────

    @Test
    @DisplayName("usuario admin: ROLE_ADMIN + el authorityCode de TODAS las acciones del sistema")
    void loadUserByUsername_admin_recibeRoleAdminYTodasLasAcciones() {
        Usuario admin = nuevoUsuario("admin", true, true, null);
        when(usuarioDao.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(accionDao.findAll()).thenReturn(List.of(
                nuevaAccion("MIEMBROS", "VER"),
                nuevaAccion("OBREROS", "DESIGNAR")));

        UserDetails detalles = service.loadUserByUsername("admin");

        assertThat(detalles.getAuthorities())
                .extracting(a -> a.getAuthority())
                .containsExactlyInAnyOrder("ROLE_ADMIN", "MIEMBROS:VER", "OBREROS:DESIGNAR");
    }

    @Test
    @DisplayName("usuario admin: no consulta los cargos del miembro, ignora esa rama por completo")
    void loadUserByUsername_admin_ignoraCargosDelMiembro() {
        Miembro miembro = new Miembro();
        miembro.setCargos(List.of(nuevoCargo(null, true, null))); // no deberia ni mirarse
        Usuario admin = nuevoUsuario("admin", true, true, miembro);
        when(usuarioDao.findByUsername("admin")).thenReturn(Optional.of(admin));
        when(accionDao.findAll()).thenReturn(List.of());

        UserDetails detalles = service.loadUserByUsername("admin");

        assertThat(detalles.getAuthorities()).extracting(a -> a.getAuthority()).containsExactly("ROLE_ADMIN");
    }

    // ───────────────────────── miembro con cargos ─────────────────────────

    @Test
    @DisplayName("cargo activo sin fecha de fin: otorga ROLE_<nombreRol> + acciones del rol")
    void loadUserByUsername_cargoActivoSinFechaFin_otorgaRolYAcciones() {
        RolCargo rol = new RolCargo();
        rol.setNombreRol("PASTOR");
        rol.setAcciones(Set.of(nuevaAccion("EVENTOS", "CREAR")));
        Miembro miembro = new Miembro();
        miembro.setCargos(List.of(nuevoCargo(rol, true, null)));
        Usuario u = nuevoUsuario("carlos", true, false, miembro);
        when(usuarioDao.findByUsername("carlos")).thenReturn(Optional.of(u));

        UserDetails detalles = service.loadUserByUsername("carlos");

        assertThat(detalles.getAuthorities())
                .extracting(a -> a.getAuthority())
                .containsExactlyInAnyOrder("ROLE_PASTOR", "EVENTOS:CREAR");
    }

    @Test
    @DisplayName("cargo activo con fecha de fin futura: sigue otorgando autoridad")
    void loadUserByUsername_cargoActivoConFechaFinFutura_otorgaAutoridad() {
        RolCargo rol = new RolCargo();
        rol.setNombreRol("PASTOR");
        Miembro miembro = new Miembro();
        Date manana = new Date(System.currentTimeMillis() + 24L * 60 * 60 * 1000);
        miembro.setCargos(List.of(nuevoCargo(rol, true, manana)));
        Usuario u = nuevoUsuario("carlos", true, false, miembro);
        when(usuarioDao.findByUsername("carlos")).thenReturn(Optional.of(u));

        UserDetails detalles = service.loadUserByUsername("carlos");

        assertThat(detalles.getAuthorities()).extracting(a -> a.getAuthority()).contains("ROLE_PASTOR");
    }

    @Test
    @DisplayName("cargo con fecha de fin vencida: NO otorga autoridad, aunque estado siga en true")
    void loadUserByUsername_cargoVencido_noOtorgaAutoridad() {
        RolCargo rol = new RolCargo();
        rol.setNombreRol("PASTOR");
        Miembro miembro = new Miembro();
        Date ayer = new Date(System.currentTimeMillis() - 24L * 60 * 60 * 1000);
        miembro.setCargos(List.of(nuevoCargo(rol, true, ayer)));
        Usuario u = nuevoUsuario("carlos", true, false, miembro);
        when(usuarioDao.findByUsername("carlos")).thenReturn(Optional.of(u));

        UserDetails detalles = service.loadUserByUsername("carlos");

        assertThat(detalles.getAuthorities()).isEmpty();
    }

    @Test
    @DisplayName("cargo inactivo (estado=false): NO otorga autoridad aunque no tenga fecha de fin")
    void loadUserByUsername_cargoInactivo_noOtorgaAutoridad() {
        RolCargo rol = new RolCargo();
        rol.setNombreRol("PASTOR");
        Miembro miembro = new Miembro();
        miembro.setCargos(List.of(nuevoCargo(rol, false, null)));
        Usuario u = nuevoUsuario("carlos", true, false, miembro);
        when(usuarioDao.findByUsername("carlos")).thenReturn(Optional.of(u));

        UserDetails detalles = service.loadUserByUsername("carlos");

        assertThat(detalles.getAuthorities()).isEmpty();
    }

    @Test
    @DisplayName("cargo activo sin rolCargo asignado: no revienta, simplemente no aporta autoridades")
    void loadUserByUsername_cargoSinRolCargo_noRevienta() {
        Miembro miembro = new Miembro();
        miembro.setCargos(List.of(nuevoCargo(null, true, null)));
        Usuario u = nuevoUsuario("carlos", true, false, miembro);
        when(usuarioDao.findByUsername("carlos")).thenReturn(Optional.of(u));

        assertThat(service.loadUserByUsername("carlos").getAuthorities()).isEmpty();
    }

    @Test
    @DisplayName("miembro sin cargos (lista null o vacia) y no admin: sin autoridades")
    void loadUserByUsername_miembroSinCargos_sinAutoridades() {
        Miembro miembro = new Miembro(); // cargos = null
        Usuario u = nuevoUsuario("carlos", true, false, miembro);
        when(usuarioDao.findByUsername("carlos")).thenReturn(Optional.of(u));

        assertThat(service.loadUserByUsername("carlos").getAuthorities()).isEmpty();
    }

    @Test
    @DisplayName("usuario sin miembro vinculado y no admin: sin autoridades, no revienta")
    void loadUserByUsername_sinMiembroVinculado_sinAutoridades() {
        Usuario u = nuevoUsuario("carlos", true, false, null);
        when(usuarioDao.findByUsername("carlos")).thenReturn(Optional.of(u));

        assertThat(service.loadUserByUsername("carlos").getAuthorities()).isEmpty();
    }

    // ───────────────────────── enabled ─────────────────────────

    @Test
    @DisplayName("usuario.estado = false: la cuenta queda deshabilitada (enabled=false)")
    void loadUserByUsername_estadoFalse_cuentaDeshabilitada() {
        Usuario u = nuevoUsuario("carlos", false, false, null);
        when(usuarioDao.findByUsername("carlos")).thenReturn(Optional.of(u));

        assertThat(service.loadUserByUsername("carlos").isEnabled()).isFalse();
    }

    @Test
    @DisplayName("usuario.estado = NULL: default-deny, la cuenta queda deshabilitada")
    void loadUserByUsername_estadoNull_defaultDeny() {
        Usuario u = nuevoUsuario("carlos", true, false, null);
        u.setEstado(null);
        when(usuarioDao.findByUsername("carlos")).thenReturn(Optional.of(u));

        assertThat(service.loadUserByUsername("carlos").isEnabled()).isFalse();
    }

    @Test
    @DisplayName("usuario.estado = true: cuenta habilitada")
    void loadUserByUsername_estadoTrue_cuentaHabilitada() {
        Usuario u = nuevoUsuario("carlos", true, false, null);
        when(usuarioDao.findByUsername("carlos")).thenReturn(Optional.of(u));

        assertThat(service.loadUserByUsername("carlos").isEnabled()).isTrue();
    }
}
