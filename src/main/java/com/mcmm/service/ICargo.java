package com.mcmm.service;

import com.mcmm.model.dto.CargoDto;

public interface ICargo {
    public Iterable<CargoDto> findAll();
    public CargoDto findById(Long id);
    public CargoDto create(CargoDto cargoDto);
    public void delete(Long id);
    public CargoDto estado(Long id);
    public CargoDto update( CargoDto cargoDto);
}
