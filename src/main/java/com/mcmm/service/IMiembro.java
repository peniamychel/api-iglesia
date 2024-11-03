package com.mcmm.service;

import com.mcmm.model.dto.MiembroDto;
import com.mcmm.model.dto.PersonaDto;

public interface IMiembro {

    public Iterable<MiembroDto> findAll();

    public MiembroDto findById(Long id);

    public MiembroDto create(MiembroDto miembroDto);

    public void delete(Long id);

    public MiembroDto estado(Long id);


    public MiembroDto update( MiembroDto miembroDto);
}
