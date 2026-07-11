package com.mcmm.service.impl;

import com.mcmm.model.dao.EventoAceptacionDao;
import com.mcmm.model.dao.EventoDao;
import com.mcmm.model.dto.eventoAceptacion.EventoAceptacionDto;
import com.mcmm.model.entity.EventoAceptacion;
import com.mcmm.model.entity.Evento;
import com.mcmm.service.IEventoAceptacion;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventoAceptacionImpl implements IEventoAceptacion {

    private final EventoAceptacionDao eventoAceptacionDao;
    private final EventoDao eventoDao;
    private final ModelMapper modelMapper = new ModelMapper();

    @Override
    @Transactional
    public EventoAceptacionDto decidir(EventoAceptacionDto dto) {
        EventoAceptacion aceptacion = eventoAceptacionDao
                .findByEventoIdAndIglesiaId(dto.getEventoId(), dto.getIglesiaId())
                .orElse(null);

        if (aceptacion == null) {
            Evento evento = eventoDao.findById(dto.getEventoId())
                    .orElseThrow(() -> new IllegalArgumentException("El evento con ID " + dto.getEventoId() + " no existe."));
            
            aceptacion = EventoAceptacion.builder()
                    .evento(evento)
                    .iglesiaId(dto.getIglesiaId())
                    .estado(dto.getEstado())
                    .build();
        } else {
            aceptacion.setEstado(dto.getEstado());
        }

        EventoAceptacion saved = eventoAceptacionDao.save(aceptacion);
        return buildDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventoAceptacionDto> findByIglesiaId(Long iglesiaId) {
        return eventoAceptacionDao.findByIglesiaId(iglesiaId).stream()
                .map(this::buildDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EventoAceptacionDto findByEventoIdAndIglesiaId(Long eventoId, Long iglesiaId) {
        return eventoAceptacionDao.findByEventoIdAndIglesiaId(eventoId, iglesiaId)
                .map(this::buildDto)
                .orElse(null);
    }

    private EventoAceptacionDto buildDto(EventoAceptacion ea) {
        return EventoAceptacionDto.builder()
                .id(ea.getId())
                .eventoId(ea.getEvento() != null ? ea.getEvento().getId() : null)
                .iglesiaId(ea.getIglesiaId())
                .estado(ea.getEstado())
                .updatedAt(ea.getUpdatedAt())
                .build();
    }
}
