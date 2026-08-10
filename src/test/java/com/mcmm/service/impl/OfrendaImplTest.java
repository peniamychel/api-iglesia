package com.mcmm.service.impl;

import com.mcmm.model.dao.IglesiaDao;
import com.mcmm.model.dao.OfrendaDao;
import com.mcmm.model.dao.UsuarioDao;
import com.mcmm.model.dto.OfrendaDto;
import com.mcmm.model.entity.Iglesia;
import com.mcmm.model.entity.Ofrenda;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * OfrendaImpl es multi-tenant: getCurrentIglesiaId() lee la iglesia del usuario
 * logueado desde el SecurityContext, y ese valor SIEMPRE gana sobre lo que
 * venga en el DTO al crear una ofrenda — si algun refactor invirtiera esa
 * prioridad, un usuario de una iglesia podria registrar ofrendas a nombre de
 * otra con solo cambiar el iglesiaId del payload. Tambien cubre que update()
 * es deliberadamente parcial (iglesia y tipoMovimiento son inmutables tras el
 * registro) y que delete() es un borrado logico (estado=false, nunca se llama
 * a dao.delete()).
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OfrendaImplTest {

    @Mock private OfrendaDao ofrendaDao;
    @Mock private IglesiaDao iglesiaDao;
    @Mock private UsuarioDao usuarioDao;

    private OfrendaImpl service;

    @BeforeEach
    void setUp() {
        service = new OfrendaImpl(ofrendaDao, iglesiaDao, usuarioDao);
    }

    @AfterEach
    void limpiarContextoDeSeguridad() {
        SecurityContextHolder.clearContext();
    }

    private void loguearComoUsuarioDeIglesia(Long iglesiaId) {
        var auth = new UsernamePasswordAuthenticationToken("carlos", null);
        auth.setDetails(Map.of("iglesiaId", iglesiaId));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private void loguearComoAdminSinIglesia() {
        var auth = new UsernamePasswordAuthenticationToken("admin", null);
        auth.setDetails(Map.of()); // sin iglesiaId
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    // ───────────────────────── findAll / findByPeriod: precedencia del tenant ─────────────────────────

    @Test
    @DisplayName("findAll: usuario de iglesia ve solo sus ofrendas")
    void findAll_usuarioDeIglesia_filtraPorSuIglesia() {
        loguearComoUsuarioDeIglesia(1L);
        when(ofrendaDao.findByIglesiaId(1L)).thenReturn(List.of(new Ofrenda()));

        assertThat(service.findAll()).hasSize(1);
        verify(ofrendaDao, never()).findAllActive();
    }

    @Test
    @DisplayName("findAll: admin sin iglesia ve todas las activas")
    void findAll_admin_veTodas() {
        loguearComoAdminSinIglesia();
        when(ofrendaDao.findAllActive()).thenReturn(List.of(new Ofrenda(), new Ofrenda()));

        assertThat(service.findAll()).hasSize(2);
        verify(ofrendaDao, never()).findByIglesiaId(any());
    }

    // ───────────────────────── create: la iglesia del contexto SIEMPRE gana ─────────────────────────

    @Test
    @DisplayName("create: usuario de iglesia — la ofrenda se asigna a SU iglesia, ignorando el iglesiaId del DTO")
    void create_usuarioDeIglesia_ignoraIglesiaIdDelDto() {
        loguearComoUsuarioDeIglesia(1L);
        Iglesia propia = new Iglesia();
        propia.setId(1L);
        when(iglesiaDao.findById(1L)).thenReturn(Optional.of(propia));
        ArgumentCaptor<Ofrenda> captor = ArgumentCaptor.forClass(Ofrenda.class);
        when(ofrendaDao.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        // El payload intenta apuntar a la iglesia 999 (ajena): no debe lograrlo.
        OfrendaDto dto = OfrendaDto.builder().iglesiaId(999L).tipoMovimiento("INGRESO").monto(100.0).build();
        service.create(dto, null);

        assertThat(captor.getValue().getIglesia().getId()).isEqualTo(1L);
        verify(iglesiaDao, never()).findById(999L);
    }

    @Test
    @DisplayName("create: admin sin iglesia — usa el iglesiaId que venga en el DTO")
    void create_admin_usaIglesiaIdDelDto() {
        loguearComoAdminSinIglesia();
        Iglesia destino = new Iglesia();
        destino.setId(5L);
        when(iglesiaDao.findById(5L)).thenReturn(Optional.of(destino));
        when(ofrendaDao.save(any(Ofrenda.class))).thenAnswer(inv -> inv.getArgument(0));

        OfrendaDto dto = OfrendaDto.builder().iglesiaId(5L).tipoMovimiento("INGRESO").monto(100.0).build();
        OfrendaDto resultado = service.create(dto, null);

        assertThat(resultado.getIglesiaId()).isEqualTo(5L);
    }

    @Test
    @DisplayName("create: sin iglesia resoluble (ni contexto ni DTO) lanza IllegalArgumentException")
    void create_sinIglesiaResoluble_lanzaIllegalArgument() {
        loguearComoAdminSinIglesia();

        OfrendaDto dto = OfrendaDto.builder().tipoMovimiento("INGRESO").monto(100.0).build();

        assertThatThrownBy(() -> service.create(dto, null)).isInstanceOf(IllegalArgumentException.class);
    }

    // ───────────────────────── update: solo monto/fecha/concepto son editables ─────────────────────────

    @Test
    @DisplayName("update: iglesia y tipoMovimiento son inmutables, aunque el DTO traiga otros valores")
    void update_iglesiaYTipoMovimiento_sonInmutables() {
        Iglesia iglesiaOriginal = new Iglesia();
        iglesiaOriginal.setId(1L);
        Ofrenda existente = new Ofrenda();
        existente.setId(10L);
        existente.setIglesia(iglesiaOriginal);
        existente.setTipoMovimiento("INGRESO");
        existente.setMonto(100.0);
        when(ofrendaDao.findById(10L)).thenReturn(Optional.of(existente));
        when(ofrendaDao.save(any(Ofrenda.class))).thenAnswer(inv -> inv.getArgument(0));

        OfrendaDto dto = OfrendaDto.builder().id(10L).monto(250.0)
                .tipoMovimiento("EGRESO") // intento de cambiarlo, debe ignorarse
                .fechaRecaudacion(new Date())
                .conceptoDetalle("Ajuste")
                .build();
        service.update(dto, null);

        assertThat(existente.getMonto()).isEqualTo(250.0); // esto si se actualiza
        assertThat(existente.getTipoMovimiento()).isEqualTo("INGRESO"); // esto no
        assertThat(existente.getIglesia().getId()).isEqualTo(1L); // esto tampoco
    }

    @Test
    @DisplayName("update: id inexistente lanza RuntimeException")
    void update_idInexistente_lanzaRuntimeException() {
        when(ofrendaDao.findById(404L)).thenReturn(Optional.empty());

        OfrendaDto dto = OfrendaDto.builder().id(404L).build();
        assertThatThrownBy(() -> service.update(dto, null)).isInstanceOf(RuntimeException.class);
    }

    // ───────────────────────── delete: borrado logico ─────────────────────────

    @Test
    @DisplayName("delete: borrado logico — pone estado=false y guarda, nunca borra el registro")
    void delete_esBorradoLogico() {
        Ofrenda existente = new Ofrenda();
        existente.setId(10L);
        existente.setEstado(true);
        when(ofrendaDao.findById(10L)).thenReturn(Optional.of(existente));

        service.delete(10L);

        assertThat(existente.getEstado()).isFalse();
        verify(ofrendaDao).save(existente);
        verify(ofrendaDao, never()).delete(any());
        verify(ofrendaDao, never()).deleteById(any());
    }

    // ───────────────────────── sumas: precedencia del tenant ─────────────────────────

    @Test
    @DisplayName("getSumByTipoAndPeriod: usuario de iglesia — suma acotada a su iglesia")
    void getSumByTipoAndPeriod_usuarioDeIglesia_acotaPorIglesia() {
        loguearComoUsuarioDeIglesia(1L);
        Date start = new Date();
        Date end = new Date();
        when(ofrendaDao.sumMontoByIglesiaAndTipoAndPeriod(1L, "INGRESO", start, end)).thenReturn(500.0);

        assertThat(service.getSumByTipoAndPeriod("INGRESO", start, end)).isEqualTo(500.0);
        verify(ofrendaDao, never()).sumMontoByTipoAndPeriod(any(), any(), any());
    }

    @Test
    @DisplayName("getSumByTipoAndPeriod: admin sin iglesia — suma global")
    void getSumByTipoAndPeriod_admin_sumaGlobal() {
        loguearComoAdminSinIglesia();
        Date start = new Date();
        Date end = new Date();
        when(ofrendaDao.sumMontoByTipoAndPeriod("INGRESO", start, end)).thenReturn(1000.0);

        assertThat(service.getSumByTipoAndPeriod("INGRESO", start, end)).isEqualTo(1000.0);
        verify(ofrendaDao, never()).sumMontoByIglesiaAndTipoAndPeriod(any(), any(), any(), any());
    }
}
