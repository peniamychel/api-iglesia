package com.mcmm.service.impl;

import com.mcmm.exception.NotFoundExceptionResource;
import com.mcmm.model.dao.CargoDao;
import com.mcmm.model.dao.EventoDao;
import com.mcmm.model.dao.ResponsableEventoDao;
import com.mcmm.model.dto.responsableEvento.ResponsableEventoDto;
import com.mcmm.model.entity.Cargo;
import com.mcmm.model.entity.Evento;
import com.mcmm.model.entity.ResponsableEvento;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Test de create()/update() de ResponsableEventoImpl.
 *
 * Cubre una regresion real: el DTO lleva nombreCompleto y nombreCargo (campos
 * de solo lectura, para mostrar en la lista), y mapear el DTO ENTERO hacia la
 * entidad con ModelMapper.map(dto, ResponsableEvento.class) fallaba con
 * ConfigurationException porque ese mapeo implicito veia dos candidatos
 * ("nombreCompleto" y "nombreCargo") para rellenar evento.nombre. El sintoma
 * en produccion era un 500 al asignar cualquier responsable.
 *
 * El ModelMapper de la clase NO esta inyectado (es `new ModelMapper()` interno),
 * asi que aqui se usa uno real: si alguien reintroduce el mapeo DTO->entidad,
 * este test reproduce el mismo ConfigurationException que veia el usuario.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ResponsableEventoImplTest {

    @Mock private ResponsableEventoDao responsableEventoDao;
    @Mock private EventoDao eventoDao;
    @Mock private CargoDao cargoDao;

    private ResponsableEventoImpl service;

    private ResponsableEventoImpl newService() {
        return new ResponsableEventoImpl(responsableEventoDao, eventoDao, cargoDao);
    }

    /** DTO tal como lo arma la lista: trae los campos de solo lectura completos. */
    private ResponsableEventoDto dtoConNombresDeSoloLectura(Long eventoId, Long cargoId) {
        ResponsableEventoDto dto = new ResponsableEventoDto();
        dto.setEventoId(eventoId);
        dto.setCargoId(cargoId);
        dto.setEstado(true);
        dto.setNombreCompleto("Sabino Rueda Lujo");
        dto.setNombreCargo("Lider de Jovenes");
        return dto;
    }

    @Test
    @DisplayName("REGRESION: crear con nombreCompleto y nombreCargo llenos no lanza ConfigurationException")
    void create_conNombresDeSoloLectura_noFalla() {
        service = newService();
        Evento evento = new Evento();
        evento.setId(1L);
        evento.setNombre("Taller Libertad 2026");
        Cargo cargo = new Cargo();
        cargo.setId(9L);

        when(eventoDao.findById(1L)).thenReturn(Optional.of(evento));
        when(cargoDao.findById(9L)).thenReturn(Optional.of(cargo));
        when(responsableEventoDao.save(org.mockito.ArgumentMatchers.any(ResponsableEvento.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        assertThatCode(() -> service.create(dtoConNombresDeSoloLectura(1L, 9L)))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Crear: resuelve evento y cargo por su id y guarda la entidad")
    void create_resuelveEventoYCargoPorId() {
        service = newService();
        Evento evento = new Evento();
        evento.setId(1L);
        Cargo cargo = new Cargo();
        cargo.setId(9L);

        when(eventoDao.findById(1L)).thenReturn(Optional.of(evento));
        when(cargoDao.findById(9L)).thenReturn(Optional.of(cargo));
        when(responsableEventoDao.save(org.mockito.ArgumentMatchers.any(ResponsableEvento.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        ResponsableEventoDto resultado = service.create(dtoConNombresDeSoloLectura(1L, 9L));

        ArgumentCaptor<ResponsableEvento> captor = ArgumentCaptor.forClass(ResponsableEvento.class);
        org.mockito.Mockito.verify(responsableEventoDao).save(captor.capture());
        assertThat(captor.getValue().getEvento()).isEqualTo(evento);
        assertThat(captor.getValue().getCargo()).isEqualTo(cargo);
        assertThat(captor.getValue().getEstado()).isTrue();

        assertThat(resultado.getEventoId()).isEqualTo(1L);
        assertThat(resultado.getCargoId()).isEqualTo(9L);
    }

    @Test
    @DisplayName("Actualizar: no toca evento ni cargo si el DTO no los trae")
    void update_sinEventoNiCargoEnElRequest_conservaLosExistentes() {
        service = newService();
        ResponsableEvento existente = new ResponsableEvento();
        existente.setId(30L);
        Evento eventoOriginal = new Evento();
        eventoOriginal.setId(1L);
        existente.setEvento(eventoOriginal);
        Cargo cargoOriginal = new Cargo();
        cargoOriginal.setId(9L);
        existente.setCargo(cargoOriginal);
        existente.setEstado(true);

        when(responsableEventoDao.findById(30L)).thenReturn(Optional.of(existente));
        when(responsableEventoDao.save(org.mockito.ArgumentMatchers.any(ResponsableEvento.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        ResponsableEventoDto dto = new ResponsableEventoDto();
        dto.setId(30L);
        dto.setEstado(false); // solo se desactiva, sin reasignar evento/cargo

        ArgumentCaptor<ResponsableEvento> captor = ArgumentCaptor.forClass(ResponsableEvento.class);
        service.update(dto);
        org.mockito.Mockito.verify(responsableEventoDao).save(captor.capture());

        assertThat(captor.getValue().getEvento()).isEqualTo(eventoOriginal);
        assertThat(captor.getValue().getCargo()).isEqualTo(cargoOriginal);
        assertThat(captor.getValue().getEstado()).isFalse();
    }

    @Test
    @DisplayName("Actualizar un id inexistente: NotFoundExceptionResource")
    void update_idInexistente_lanzaNotFound() {
        service = newService();
        when(responsableEventoDao.findById(404L)).thenReturn(Optional.empty());

        ResponsableEventoDto dto = new ResponsableEventoDto();
        dto.setId(404L);

        assertThatThrownBy(() -> service.update(dto))
                .isInstanceOf(NotFoundExceptionResource.class);
    }
}
