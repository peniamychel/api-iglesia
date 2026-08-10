package com.mcmm.service.impl;

import com.mcmm.exception.NotFoundExceptionResource;
import com.mcmm.model.dao.ServicioDao;
import com.mcmm.model.dto.ServicioDto;
import com.mcmm.model.entity.Servicio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ServicioImplTest {

    @Mock private ServicioDao servicioDao;

    private ServicioImpl service;

    @BeforeEach
    void setUp() {
        service = new ServicioImpl(servicioDao);
    }

    @Test
    @DisplayName("update: activo=null en el DTO preserva el existente")
    void update_activoNull_preservaElExistente() {
        Servicio existente = new Servicio();
        existente.setId(1L);
        existente.setActivo(true);
        when(servicioDao.findById(1L)).thenReturn(Optional.of(existente));
        when(servicioDao.save(any(Servicio.class))).thenAnswer(inv -> inv.getArgument(0));

        ServicioDto dto = ServicioDto.builder().nombre("Miembros").codigo("MIEMBROS").activo(null).build();
        service.update(1L, dto);

        assertThat(existente.getActivo()).isTrue();
    }

    @Test
    @DisplayName("update: activo explicito se aplica")
    void update_activoExplicito_seAplica() {
        Servicio existente = new Servicio();
        existente.setId(1L);
        existente.setActivo(true);
        when(servicioDao.findById(1L)).thenReturn(Optional.of(existente));
        when(servicioDao.save(any(Servicio.class))).thenAnswer(inv -> inv.getArgument(0));

        ServicioDto dto = ServicioDto.builder().nombre("Miembros").codigo("MIEMBROS").activo(false).build();
        service.update(1L, dto);

        assertThat(existente.getActivo()).isFalse();
    }

    @Test
    @DisplayName("update: id inexistente lanza NotFoundExceptionResource")
    void update_idInexistente_lanzaNotFound() {
        when(servicioDao.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(404L, ServicioDto.builder().build()))
                .isInstanceOf(NotFoundExceptionResource.class);
    }

    @Test
    @DisplayName("findById: id inexistente lanza NotFoundExceptionResource")
    void findById_idInexistente_lanzaNotFound() {
        when(servicioDao.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(404L)).isInstanceOf(NotFoundExceptionResource.class);
    }

    @Test
    @DisplayName("delete: id inexistente lanza NotFoundExceptionResource, no llama delete")
    void delete_idInexistente_lanzaNotFound() {
        when(servicioDao.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(404L)).isInstanceOf(NotFoundExceptionResource.class);
        verify(servicioDao, never()).delete(any());
    }

    @Test
    @DisplayName("findAll: delega en el DAO ordenado por orden ascendente, solo activos")
    void findAll_delegaEnElDaoActivosOrdenados() {
        Servicio s = new Servicio();
        s.setId(1L);
        s.setCodigo("MIEMBROS");
        when(servicioDao.findByActivoTrueOrderByOrdenAsc()).thenReturn(java.util.List.of(s));

        assertThat(service.findAll()).hasSize(1);
    }
}
