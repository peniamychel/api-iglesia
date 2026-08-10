package com.mcmm.service.impl;

import com.mcmm.exception.BadRequestException;
import com.mcmm.exception.NotFoundExceptionResource;
import com.mcmm.model.dao.ActivoDao;
import com.mcmm.model.dao.IglesiaDao;
import com.mcmm.model.dto.ActivoDto;
import com.mcmm.model.entity.Activo;
import com.mcmm.model.entity.Iglesia;
import com.mcmm.service.FileStorageService;
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

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

/**
 * verificarPropiedad() es la barrera multi-tenant de este servicio: un rol de
 * iglesia solo puede ver/editar/borrar los bienes de SU iglesia. Cubre el
 * mismo patron de riesgo que OfrendaImpl — la iglesia del token siempre debe
 * ganar sobre lo que venga en el payload al guardar o actualizar.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ActivoImplTest {

    @Mock private ActivoDao activoDao;
    @Mock private IglesiaDao iglesiaDao;
    @Mock private ModelMapper modelMapper;
    @Mock private FileStorageService fileStorageService;

    private ActivoImpl service;

    @BeforeEach
    void setUp() {
        service = new ActivoImpl(activoDao, iglesiaDao, modelMapper, fileStorageService);
        when(modelMapper.map(any(Activo.class), eq(ActivoDto.class)))
                .thenAnswer(inv -> ActivoDto.builder().build());
        when(modelMapper.map(any(ActivoDto.class), eq(Activo.class)))
                .thenAnswer(inv -> new Activo());
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
        auth.setDetails(Map.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private Activo nuevoActivo(Long id, Long iglesiaId) {
        Activo a = new Activo();
        a.setId(id);
        if (iglesiaId != null) {
            Iglesia i = new Iglesia();
            i.setId(iglesiaId);
            a.setIglesia(i);
        }
        return a;
    }

    // ───────────────────────── findById: aislamiento por tenant ─────────────────────────

    @Test
    @DisplayName("findById: activo de otra iglesia distinta a la del token, rechaza con 400")
    void findById_activoDeOtraIglesia_lanzaBadRequest() {
        loguearComoUsuarioDeIglesia(1L);
        when(activoDao.findById(10L)).thenReturn(Optional.of(nuevoActivo(10L, 2L)));

        assertThatThrownBy(() -> service.findById(10L)).isInstanceOf(BadRequestException.class);
    }

    @Test
    @DisplayName("findById: activo de la propia iglesia, permitido")
    void findById_activoDeLaPropiaIglesia_permitido() {
        loguearComoUsuarioDeIglesia(1L);
        when(activoDao.findById(10L)).thenReturn(Optional.of(nuevoActivo(10L, 1L)));

        assertThat(service.findById(10L)).isNotNull();
    }

    @Test
    @DisplayName("findById: admin sin iglesia puede ver cualquier activo")
    void findById_admin_veCualquierActivo() {
        loguearComoAdminSinIglesia();
        when(activoDao.findById(10L)).thenReturn(Optional.of(nuevoActivo(10L, 2L)));

        assertThat(service.findById(10L)).isNotNull();
    }

    @Test
    @DisplayName("findById: id inexistente lanza NotFoundExceptionResource")
    void findById_idInexistente_lanzaNotFound() {
        loguearComoAdminSinIglesia();
        when(activoDao.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(404L)).isInstanceOf(NotFoundExceptionResource.class);
    }

    // ───────────────────────── findByIglesia ─────────────────────────

    @Test
    @DisplayName("findByIglesia: pedir la iglesia de otro, rechaza con 400")
    void findByIglesia_iglesiaAjena_lanzaBadRequest() {
        loguearComoUsuarioDeIglesia(1L);

        assertThatThrownBy(() -> service.findByIglesia(2L)).isInstanceOf(BadRequestException.class);
    }

    // ───────────────────────── save: la iglesia del token siempre gana ─────────────────────────

    @Test
    @DisplayName("save: usuario de iglesia — el activo se registra en SU iglesia, ignorando el iglesiaId del DTO")
    void save_usuarioDeIglesia_ignoraIglesiaIdDelDto() {
        loguearComoUsuarioDeIglesia(1L);
        Iglesia propia = new Iglesia();
        propia.setId(1L);
        when(iglesiaDao.findById(1L)).thenReturn(Optional.of(propia));
        ArgumentCaptor<Activo> captor = ArgumentCaptor.forClass(Activo.class);
        when(activoDao.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        ActivoDto dto = ActivoDto.builder().iglesiaId(999L).nombre("Proyector").build();
        service.save(dto);

        assertThat(captor.getValue().getIglesia().getId()).isEqualTo(1L);
        verify_neverBuscoIglesiaAjena();
    }

    private void verify_neverBuscoIglesiaAjena() {
        org.mockito.Mockito.verify(iglesiaDao, never()).findById(999L);
    }

    @Test
    @DisplayName("save: admin sin iglesia — usa el iglesiaId del DTO")
    void save_admin_usaIglesiaIdDelDto() {
        loguearComoAdminSinIglesia();
        Iglesia destino = new Iglesia();
        destino.setId(5L);
        when(iglesiaDao.findById(5L)).thenReturn(Optional.of(destino));
        when(activoDao.save(any(Activo.class))).thenAnswer(inv -> inv.getArgument(0));

        ActivoDto dto = ActivoDto.builder().iglesiaId(5L).nombre("Proyector").build();
        assertThat(service.save(dto)).isNotNull();
    }

    // ───────────────────────── update ─────────────────────────

    @Test
    @DisplayName("update: no puede reasignar el bien a otra iglesia, se fuerza la del token")
    void update_noPuedeReasignarAOtraIglesia() {
        loguearComoUsuarioDeIglesia(1L);
        Activo existente = nuevoActivo(10L, 1L);
        when(activoDao.findById(10L)).thenReturn(Optional.of(existente));
        Iglesia propia = new Iglesia();
        propia.setId(1L);
        when(iglesiaDao.findById(1L)).thenReturn(Optional.of(propia));
        when(activoDao.save(any(Activo.class))).thenAnswer(inv -> inv.getArgument(0));

        // El payload intenta mover el bien a la iglesia 999.
        ActivoDto dto = ActivoDto.builder().id(10L).iglesiaId(999L).nombre("Proyector").build();
        service.update(dto);

        assertThat(existente.getIglesia().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("update: activo de otra iglesia, rechaza antes de tocar nada")
    void update_activoDeOtraIglesia_lanzaBadRequest() {
        loguearComoUsuarioDeIglesia(1L);
        when(activoDao.findById(10L)).thenReturn(Optional.of(nuevoActivo(10L, 2L)));

        ActivoDto dto = ActivoDto.builder().id(10L).build();
        assertThatThrownBy(() -> service.update(dto)).isInstanceOf(BadRequestException.class);
        verify_nuncaGuarda();
    }

    private void verify_nuncaGuarda() {
        org.mockito.Mockito.verify(activoDao, never()).save(any());
    }

    // ───────────────────────── delete ─────────────────────────

    @Test
    @DisplayName("delete: activo de otra iglesia, rechaza sin borrar")
    void delete_activoDeOtraIglesia_lanzaBadRequestSinBorrar() {
        loguearComoUsuarioDeIglesia(1L);
        when(activoDao.findById(10L)).thenReturn(Optional.of(nuevoActivo(10L, 2L)));

        assertThatThrownBy(() -> service.delete(10L)).isInstanceOf(BadRequestException.class);
        org.mockito.Mockito.verify(activoDao, never()).delete(any());
    }

    // ───────────────────────── deletePhoto ─────────────────────────

    @Test
    @DisplayName("deletePhoto: si falla borrar el archivo fisico, no propaga y limpia igual el campo")
    void deletePhoto_fallaAlBorrarArchivo_noPropagaYLimpiaElCampo() throws java.io.IOException {
        loguearComoAdminSinIglesia();
        Activo existente = nuevoActivo(10L, null);
        existente.setUriFoto("activos/foto.jpg");
        when(activoDao.findById(10L)).thenReturn(Optional.of(existente));
        org.mockito.Mockito.doThrow(new java.io.IOException("no existe"))
                .when(fileStorageService).deleteFile(org.mockito.ArgumentMatchers.anyString());

        service.deletePhoto(10L);

        assertThat(existente.getUriFoto()).isNull();
    }
}
