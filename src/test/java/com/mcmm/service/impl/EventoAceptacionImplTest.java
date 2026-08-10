package com.mcmm.service.impl;

import com.mcmm.model.dao.EventoAceptacionDao;
import com.mcmm.model.dao.EventoDao;
import com.mcmm.model.dto.eventoAceptacion.EventoAceptacionDto;
import com.mcmm.model.entity.Evento;
import com.mcmm.model.entity.EventoAceptacion;
import org.junit.jupiter.api.BeforeEach;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EventoAceptacionImplTest {

    @Mock private EventoAceptacionDao eventoAceptacionDao;
    @Mock private EventoDao eventoDao;

    private EventoAceptacionImpl service;

    @BeforeEach
    void setUp() {
        service = new EventoAceptacionImpl(eventoAceptacionDao, eventoDao);
    }

    @Test
    @DisplayName("decidir: sin decision previa, crea una nueva luego de validar que el evento existe")
    void decidir_sinDecisionPrevia_creaUnaNueva() {
        Evento evento = new Evento();
        evento.setId(1L);
        when(eventoAceptacionDao.findByEventoIdAndIglesiaId(1L, 2L)).thenReturn(Optional.empty());
        when(eventoDao.findById(1L)).thenReturn(Optional.of(evento));
        ArgumentCaptor<EventoAceptacion> captor = ArgumentCaptor.forClass(EventoAceptacion.class);
        when(eventoAceptacionDao.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        EventoAceptacionDto dto = EventoAceptacionDto.builder().eventoId(1L).iglesiaId(2L).estado("ACEPTADO").build();
        service.decidir(dto);

        assertThat(captor.getValue().getEvento().getId()).isEqualTo(1L);
        assertThat(captor.getValue().getIglesiaId()).isEqualTo(2L);
        assertThat(captor.getValue().getEstado()).isEqualTo("ACEPTADO");
    }

    @Test
    @DisplayName("decidir: evento inexistente lanza IllegalArgumentException")
    void decidir_eventoInexistente_lanzaIllegalArgument() {
        when(eventoAceptacionDao.findByEventoIdAndIglesiaId(99L, 2L)).thenReturn(Optional.empty());
        when(eventoDao.findById(99L)).thenReturn(Optional.empty());

        EventoAceptacionDto dto = EventoAceptacionDto.builder().eventoId(99L).iglesiaId(2L).estado("ACEPTADO").build();
        assertThatThrownBy(() -> service.decidir(dto)).isInstanceOf(IllegalArgumentException.class);
        verify(eventoAceptacionDao, never()).save(any());
    }

    @Test
    @DisplayName("decidir: con decision previa, solo actualiza el estado sin re-validar el evento")
    void decidir_conDecisionPrevia_soloActualizaEstado() {
        Evento evento = new Evento();
        evento.setId(1L);
        EventoAceptacion existente = EventoAceptacion.builder().id(50L).evento(evento).iglesiaId(2L).estado("ACEPTADO").build();
        when(eventoAceptacionDao.findByEventoIdAndIglesiaId(1L, 2L)).thenReturn(Optional.of(existente));
        when(eventoAceptacionDao.save(any(EventoAceptacion.class))).thenAnswer(inv -> inv.getArgument(0));

        EventoAceptacionDto dto = EventoAceptacionDto.builder().eventoId(1L).iglesiaId(2L).estado("ARCHIVADO").build();
        EventoAceptacionDto resultado = service.decidir(dto);

        assertThat(resultado.getEstado()).isEqualTo("ARCHIVADO");
        assertThat(resultado.getId()).isEqualTo(50L); // sigue siendo el mismo registro, no uno nuevo
        verify(eventoDao, never()).findById(any());
    }

    @Test
    @DisplayName("findByEventoIdAndIglesiaId: sin decision, devuelve null en vez de lanzar")
    void findByEventoIdAndIglesiaId_sinDecision_devuelveNull() {
        when(eventoAceptacionDao.findByEventoIdAndIglesiaId(1L, 2L)).thenReturn(Optional.empty());

        assertThat(service.findByEventoIdAndIglesiaId(1L, 2L)).isNull();
    }

    @Test
    @DisplayName("findByIglesiaId: mapea la lista completa de decisiones de la iglesia")
    void findByIglesiaId_mapeaLaLista() {
        Evento evento = new Evento();
        evento.setId(1L);
        EventoAceptacion ea = EventoAceptacion.builder().id(1L).evento(evento).iglesiaId(2L).estado("ACEPTADO").build();
        when(eventoAceptacionDao.findByIglesiaId(2L)).thenReturn(java.util.List.of(ea));

        assertThat(service.findByIglesiaId(2L)).hasSize(1);
    }
}
