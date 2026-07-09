package com.mcmm.service;

import com.mcmm.model.dto.RolCargoDto;

import java.util.List;

public interface IRolCargo {
    List<RolCargoDto> findAll();

    List<RolCargoDto> findAllCargo();

    RolCargoDto findById(Long id);

    RolCargoDto create(RolCargoDto rolCargoDto);

    RolCargoDto update(RolCargoDto rolCargoDto);

    void delete(Long id);

    void estado(Long id);

    RolCargoDto addAccion(Long rolCargoId, Long accionId);

    RolCargoDto removeAccion(Long rolCargoId, Long accionId);
}
