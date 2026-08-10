package com.mcmm.service.impl;

import com.mcmm.model.dao.BitacoraDao;
import com.mcmm.model.dao.UsuarioDao;
import com.mcmm.model.dto.BitacoraDto;
import com.mcmm.model.entity.Bitacora;
import com.mcmm.model.entity.Usuario;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * registrarAccion() es logging de auditoria de mejor esfuerzo: NUNCA debe
 * romper el flujo principal de la app, aunque falle resolver el usuario, no
 * haya un HttpServletRequest activo (fuera de un request real, como en este
 * test) o cualquier otra cosa salga mal — por eso el try/catch que lo envuelve
 * en produccion se prueba explicitamente aqui.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BitacoraImplTest {

    @Mock private BitacoraDao bitacoraDao;
    @Mock private UsuarioDao usuarioDao;
    @Mock private ModelMapper modelMapper;

    private BitacoraImpl service;

    @BeforeEach
    void setUp() {
        service = new BitacoraImpl(bitacoraDao, usuarioDao, modelMapper);
        when(modelMapper.map(any(Bitacora.class), eq(BitacoraDto.class)))
                .thenAnswer(inv -> new BitacoraDto());
    }

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
        RequestContextHolder.resetRequestAttributes();
    }

    // ───────────────────────── registrar ─────────────────────────

    @Test
    @DisplayName("registrar: con usuarioId, resuelve el usuario por id y usa SU username, no el pasado por parametro")
    void registrar_conUsuarioId_usaElUsernameDelUsuarioResuelto() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setUsername("carlos.real");
        when(usuarioDao.findById(1L)).thenReturn(Optional.of(usuario));
        ArgumentCaptor<Bitacora> captor = ArgumentCaptor.forClass(Bitacora.class);
        when(bitacoraDao.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        service.registrar(1L, "nombre-obsoleto", "CREAR", "MIEMBRO", "desc", "127.0.0.1");

        assertThat(captor.getValue().getUsername()).isEqualTo("carlos.real");
        assertThat(captor.getValue().getUsuario()).isEqualTo(usuario);
    }

    @Test
    @DisplayName("registrar: sin usuarioId, resuelve por username")
    void registrar_sinUsuarioId_resuelvePorUsername() {
        Usuario usuario = new Usuario();
        usuario.setUsername("carlos");
        when(usuarioDao.findByUsername("carlos")).thenReturn(Optional.of(usuario));
        when(bitacoraDao.save(any(Bitacora.class))).thenAnswer(inv -> inv.getArgument(0));

        service.registrar(null, "carlos", "CREAR", "MIEMBRO", "desc", "127.0.0.1");

        verify(usuarioDao).findByUsername("carlos");
        verify(usuarioDao, never()).findById(any());
    }

    @Test
    @DisplayName("registrar: usuario no resoluble, igual guarda el registro con el username crudo")
    void registrar_usuarioNoResoluble_guardaConUsernameCrudo() {
        when(usuarioDao.findByUsername("fantasma")).thenReturn(Optional.empty());
        ArgumentCaptor<Bitacora> captor = ArgumentCaptor.forClass(Bitacora.class);
        when(bitacoraDao.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        service.registrar(null, "fantasma", "CREAR", "MIEMBRO", "desc", "127.0.0.1");

        assertThat(captor.getValue().getUsername()).isEqualTo("fantasma");
        assertThat(captor.getValue().getUsuario()).isNull();
    }

    // ───────────────────────── registrarAccion: nunca rompe el flujo principal ─────────────────────────

    @Test
    @DisplayName("registrarAccion: sin request HTTP activo (como en este test), no lanza excepcion")
    void registrarAccion_sinRequestActivo_noLanza() {
        var auth = new UsernamePasswordAuthenticationToken("carlos", null, java.util.List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
        when(usuarioDao.findByUsername("carlos")).thenReturn(Optional.empty());
        when(bitacoraDao.save(any(Bitacora.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThatCode(() -> service.registrarAccion("MIEMBRO", "CREAR", "desc")).doesNotThrowAnyException();

        verify(bitacoraDao).save(any());
    }

    @Test
    @DisplayName("registrarAccion: sin autenticacion, usa 'Sistema' como usuario")
    void registrarAccion_sinAutenticacion_usaSistema() {
        SecurityContextHolder.clearContext();
        ArgumentCaptor<Bitacora> captor = ArgumentCaptor.forClass(Bitacora.class);
        when(bitacoraDao.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        service.registrarAccion("MIEMBRO", "CREAR", "desc");

        assertThat(captor.getValue().getUsername()).isEqualTo("Sistema");
    }

    @Test
    @DisplayName("registrarAccion: un error inesperado al registrar se traga por completo, no interrumpe al caller")
    void registrarAccion_errorInesperado_seTraga() {
        when(bitacoraDao.save(any(Bitacora.class))).thenThrow(new RuntimeException("BD caida"));

        assertThatCode(() -> service.registrarAccion("MIEMBRO", "CREAR", "desc")).doesNotThrowAnyException();
    }

    // ───────────────────────── convertToDto (via findAll) ─────────────────────────

    @Test
    @DisplayName("findAll: con usuario vinculado, arma el nombre completo desde el usuario")
    void findAll_conUsuarioVinculado_armaNombreCompleto() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setName("Carlos");
        usuario.setApellidos("Perez");
        Bitacora b = Bitacora.builder().usuario(usuario).build();
        when(bitacoraDao.findAllByOrderByFechaDesc()).thenReturn(java.util.List.of(b));

        BitacoraDto dto = service.findAll().get(0);

        assertThat(dto.getUserFullName()).isEqualTo("Carlos Perez");
    }

    @Test
    @DisplayName("findAll: sin usuario vinculado pero con username, usa el username")
    void findAll_sinUsuarioConUsername_usaElUsername() {
        Bitacora b = Bitacora.builder().username("carlos").build();
        when(bitacoraDao.findAllByOrderByFechaDesc()).thenReturn(java.util.List.of(b));

        assertThat(service.findAll().get(0).getUserFullName()).isEqualTo("carlos");
    }

    @Test
    @DisplayName("findAll: sin usuario ni username, cae a 'Sistema'")
    void findAll_sinUsuarioNiUsername_caeASistema() {
        Bitacora b = Bitacora.builder().build();
        when(bitacoraDao.findAllByOrderByFechaDesc()).thenReturn(java.util.List.of(b));

        assertThat(service.findAll().get(0).getUserFullName()).isEqualTo("Sistema");
    }
}
