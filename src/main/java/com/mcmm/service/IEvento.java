package com.mcmm.service;

import com.mcmm.model.dto.evento.EventoDto;

import java.util.List;

public interface IEvento {
    List<EventoDto> findAll();
    EventoDto findById(Long id);
    EventoDto create(EventoDto eventoDto);
    EventoDto update(EventoDto eventoDto);
    void delete(Long id);
    void estado(Long id);
    void cloneYearEvents(int fromYear, int toYear);
}