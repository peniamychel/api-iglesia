package com.mcmm.service;

import com.mcmm.model.dto.MiembroDto;
import com.mcmm.model.dto.PersonaDto;

public interface IMiembro {

    public Iterable<MiembroDto> findAll();

    public MiembroDto findById(Long id);

    public MiembroDto save(MiembroDto miembroDto);

    public void delete(MiembroDto miembroDto);

    public MiembroDto update(Long id, MiembroDto miembroDto);
}
