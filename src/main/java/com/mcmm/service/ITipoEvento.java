package com.mcmm.service;

import com.mcmm.model.dto.tipoEvento.TipoEventoDto;

import java.util.List;

public interface ITipoEvento {
    List<TipoEventoDto> findAll();
    TipoEventoDto findById(Long id);
    TipoEventoDto create(TipoEventoDto tipoEventoDto);
    TipoEventoDto update(TipoEventoDto tipoEventoDto);
    void delete(Long id);
    void estado(Long id);
}