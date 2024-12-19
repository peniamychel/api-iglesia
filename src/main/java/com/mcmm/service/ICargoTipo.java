package com.mcmm.service;

import com.mcmm.model.dto.CargoTipoDto;
import com.mcmm.model.entity.CargoTipo;

public interface ICargoTipo {
    public Iterable<CargoTipoDto> findAll();
    public CargoTipoDto findById(Long id);
    public CargoTipoDto create(CargoTipoDto cargoTipoDto);
    public void delete(Long id);
    public CargoTipoDto estado(Long id);
    public CargoTipoDto update( CargoTipoDto cargoTipoDto);
}
