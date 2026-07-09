package com.mcmm.service;

import com.mcmm.model.dto.AccionDto;

import java.util.List;

public interface IAccion {
    List<AccionDto> findAll();
    List<AccionDto> findByServicioId(Long servicioId);
    AccionDto findById(Long id);
    AccionDto create(AccionDto accionDto);
    AccionDto update(Long id, AccionDto accionDto);
    void delete(Long id);
}
