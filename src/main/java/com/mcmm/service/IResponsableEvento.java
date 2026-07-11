package com.mcmm.service;

import com.mcmm.model.dto.responsableEvento.ResponsableEventoDto;

import java.util.List;

public interface IResponsableEvento {
    List<ResponsableEventoDto> findAll();
    List<ResponsableEventoDto> findByEventoId(Long eventoId);
    ResponsableEventoDto findById(Long id);
    ResponsableEventoDto create(ResponsableEventoDto responsableEventoDto);
    ResponsableEventoDto update(ResponsableEventoDto responsableEventoDto);
    void delete(Long id);
    void estado(Long id);
}