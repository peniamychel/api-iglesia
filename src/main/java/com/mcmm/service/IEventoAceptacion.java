package com.mcmm.service;

import com.mcmm.model.dto.eventoAceptacion.EventoAceptacionDto;
import java.util.List;

public interface IEventoAceptacion {
    EventoAceptacionDto decidir(EventoAceptacionDto dto);
    List<EventoAceptacionDto> findByIglesiaId(Long iglesiaId);
    EventoAceptacionDto findByEventoIdAndIglesiaId(Long eventoId, Long iglesiaId);
}
