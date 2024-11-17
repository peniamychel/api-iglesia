package com.mcmm.service;

import com.mcmm.model.dto.*;

import java.util.List;

public interface IMiembroIglesia {

    public Iterable<MiembroIglesiaDto> findAll();

    public MiembroIglesiaDto findById(Long id);

    public MiembroIglesiaDto save(MiembroIglesiaDto miembroIglesiaDto);

    public void delete(MiembroIglesiaDto miembroIglesiaDto);

    public MiembroIglesiaDto update(MiembroIglesiaDto miembroIglesiaDto);

    public boolean estado(Long id);

    Iterable<MiembroDto> findMiembrosIglesia(Long id);

    boolean findByIdMiembro(Long id);

    List<GraficoDataDto> graficoMiembrosIglesia(Long cant);
}
