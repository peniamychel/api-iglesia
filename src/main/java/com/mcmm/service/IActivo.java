package com.mcmm.service;

import com.mcmm.model.dto.ActivoDto;
import java.util.List;

public interface IActivo {
    List<ActivoDto> findAll();
    List<ActivoDto> findByIglesia(Long iglesiaId);
    ActivoDto findById(Long id);
    ActivoDto save(ActivoDto activoDto);
    ActivoDto update(ActivoDto activoDto);
    void delete(Long id);
}
