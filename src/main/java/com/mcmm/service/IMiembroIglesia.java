package com.mcmm.service;

import com.mcmm.model.dto.MiembroIglesiaDto;
import com.mcmm.model.dto.PersonaDto;

public interface IMiembroIglesia {

    public Iterable<MiembroIglesiaDto> findAll();

    public MiembroIglesiaDto findById(Long id);

    public MiembroIglesiaDto save(MiembroIglesiaDto miembroIglesiaDto);

    public void delete(MiembroIglesiaDto miembroIglesiaDto);

    public MiembroIglesiaDto update(Long id, MiembroIglesiaDto miembroIglesiaDto);
}
