package com.mcmm.service.impl;

import com.mcmm.model.dao.TipoEventoDao;
import com.mcmm.model.dto.tipoEvento.TipoEventoDto;
import com.mcmm.model.entity.TipoEvento;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * A diferencia del resto de los Impl de catalogo (RolCargoImpl, AccionImpl,
 * ServicioImpl), update() aqui NO carga la entidad existente antes de guardar
 * — mapea el DTO directo a una entidad nueva con el mismo id y la guarda tal
 * cual. Con solo "nombre"/"estado" como campos reales el riesgo practico es
 * bajo, pero significa que un update() con "estado" ausente en el payload lo
 * deja en null. Se documenta con un test, sin tocar produccion.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TipoEventoImplTest {

    @Mock private TipoEventoDao tipoEventoDao;

    private TipoEventoImpl service;

    @BeforeEach
    void setUp() {
        service = new TipoEventoImpl(tipoEventoDao);
    }

    @Test
    @DisplayName("findById: id inexistente devuelve null, no lanza (distinto del resto de la app)")
    void findById_idInexistente_devuelveNull() {
        when(tipoEventoDao.findById(404L)).thenReturn(Optional.empty());

        assertThat(service.findById(404L)).isNull();
    }

    @Test
    @DisplayName("findById: id existente mapea correctamente")
    void findById_idExistente_mapeaCorrectamente() {
        TipoEvento entidad = new TipoEvento();
        entidad.setId(1L);
        entidad.setNombre("Retiro");
        when(tipoEventoDao.findById(1L)).thenReturn(Optional.of(entidad));

        TipoEventoDto dto = service.findById(1L);

        assertThat(dto.getNombre()).isEqualTo("Retiro");
    }

    @Test
    @DisplayName("create: guarda el mapeo directo del DTO")
    void create_guardaElMapeoDirecto() {
        when(tipoEventoDao.save(any(TipoEvento.class))).thenAnswer(inv -> inv.getArgument(0));

        TipoEventoDto dto = service.create(TipoEventoDto.builder().nombre("Congreso").build());

        assertThat(dto.getNombre()).isEqualTo("Congreso");
    }

    @Test
    @DisplayName("update: un DTO sin 'estado' lo deja en null al guardar (no hay fetch-first)")
    void update_sinEstadoEnElDto_loDejaEnNull() {
        when(tipoEventoDao.save(any(TipoEvento.class))).thenAnswer(inv -> inv.getArgument(0));

        TipoEventoDto dto = service.update(TipoEventoDto.builder().id(1L).nombre("Congreso").build());

        assertThat(dto.getEstado()).isNull();
    }

    @Test
    @DisplayName("delete: delega directo en deleteById, sin verificar existencia previa")
    void delete_delegaEnDeleteById() {
        service.delete(1L);

        verify(tipoEventoDao).deleteById(1L);
    }

    @Test
    @DisplayName("estado: delega en toggleEstado del DAO")
    void estado_delegaEnToggleEstado() {
        service.estado(1L);

        verify(tipoEventoDao).toggleEstado(1L);
    }
}
