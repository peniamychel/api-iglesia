package com.mcmm.service;

import com.mcmm.model.dto.CargoTipoDto;

public interface ICargoTipo {
    public Iterable<CargoTipoDto> findAll();

    public CargoTipoDto findById(Long id);

    public CargoTipoDto create(CargoTipoDto cargoTipoDto);

    public void delete(Long id);

    public void estado(Long id);

    public CargoTipoDto update(CargoTipoDto cargoTipoDto);

    public CargoTipoDto save(CargoTipoDto cargoTipoDto);

}
