package com.mcmm.service.impl;

import com.mcmm.exception.NotFoundExceptionResource;
import com.mcmm.model.dao.AccionDao;
import com.mcmm.model.dao.ServicioDao;
import com.mcmm.model.dto.AccionDto;
import com.mcmm.model.entity.Accion;
import com.mcmm.model.entity.Servicio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AccionImplTest {

    @Mock private AccionDao accionDao;
    @Mock private ServicioDao servicioDao;

    private AccionImpl service;

    @BeforeEach
    void setUp() {
        service = new AccionImpl(accionDao, servicioDao);
    }

    private Servicio nuevoServicio(Long id, String codigo) {
        Servicio s = new Servicio();
        s.setId(id);
        s.setCodigo(codigo);
        return s;
    }

    @Test
    @DisplayName("create: arma el authorityCode como SERVICIO:CODIGO usando el servicio resuelto")
    void create_armaAuthorityCode() {
        Servicio servicio = nuevoServicio(1L, "MIEMBROS");
        when(servicioDao.findById(1L)).thenReturn(Optional.of(servicio));
        when(accionDao.save(any(Accion.class))).thenAnswer(inv -> inv.getArgument(0));

        AccionDto dto = AccionDto.builder().servicioId(1L).codigo("VER").nombre("Ver miembros").build();
        AccionDto resultado = service.create(dto);

        assertThat(resultado.getAuthorityCode()).isEqualTo("MIEMBROS:VER");
        assertThat(resultado.getServicioCodigo()).isEqualTo("MIEMBROS");
    }

    @Test
    @DisplayName("create: servicio inexistente lanza NotFoundExceptionResource, no guarda nada")
    void create_servicioInexistente_lanzaNotFound() {
        when(servicioDao.findById(99L)).thenReturn(Optional.empty());

        AccionDto dto = AccionDto.builder().servicioId(99L).codigo("VER").build();
        assertThatThrownBy(() -> service.create(dto)).isInstanceOf(NotFoundExceptionResource.class);
    }

    @Test
    @DisplayName("update: activo=null en el DTO preserva el valor existente")
    void update_activoNull_preservaElExistente() {
        Accion existente = new Accion();
        existente.setId(1L);
        existente.setActivo(true);
        when(accionDao.findById(1L)).thenReturn(Optional.of(existente));
        when(accionDao.save(any(Accion.class))).thenAnswer(inv -> inv.getArgument(0));

        AccionDto dto = AccionDto.builder().codigo("VER").nombre("Ver").activo(null).build();
        service.update(1L, dto);

        assertThat(existente.getActivo()).isTrue();
    }

    @Test
    @DisplayName("update: activo explicito en el DTO si se aplica")
    void update_activoExplicito_seAplica() {
        Accion existente = new Accion();
        existente.setId(1L);
        existente.setActivo(true);
        when(accionDao.findById(1L)).thenReturn(Optional.of(existente));
        when(accionDao.save(any(Accion.class))).thenAnswer(inv -> inv.getArgument(0));

        AccionDto dto = AccionDto.builder().codigo("VER").nombre("Ver").activo(false).build();
        service.update(1L, dto);

        assertThat(existente.getActivo()).isFalse();
    }

    @Test
    @DisplayName("update: id inexistente lanza NotFoundExceptionResource")
    void update_idInexistente_lanzaNotFound() {
        when(accionDao.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(404L, AccionDto.builder().build()))
                .isInstanceOf(NotFoundExceptionResource.class);
    }

    @Test
    @DisplayName("findById: id inexistente lanza NotFoundExceptionResource")
    void findById_idInexistente_lanzaNotFound() {
        when(accionDao.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(404L)).isInstanceOf(NotFoundExceptionResource.class);
    }

    @Test
    @DisplayName("delete: id inexistente lanza NotFoundExceptionResource, no llama delete")
    void delete_idInexistente_lanzaNotFound() {
        when(accionDao.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(404L)).isInstanceOf(NotFoundExceptionResource.class);
    }
}
