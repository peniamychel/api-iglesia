package com.mcmm.service;

import com.mcmm.model.dto.CargoTipoDto;

import java.util.List;

public interface ICargoTipo {
    List<CargoTipoDto> findAll();

    CargoTipoDto findById(Long id);

    CargoTipoDto create(CargoTipoDto cargoTipoDto);

    void delete(Long id);

    CargoTipoDto estado(Long id);

    CargoTipoDto update(CargoTipoDto cargoTipoDto);

    CargoTipoDto save(CargoTipoDto cargoTipoDto);
}
