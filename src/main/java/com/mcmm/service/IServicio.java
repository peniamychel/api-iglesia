package com.mcmm.service;

import com.mcmm.model.dto.ServicioDto;

import java.util.List;

public interface IServicio {
    List<ServicioDto> findAll();
    ServicioDto findById(Long id);
    ServicioDto create(ServicioDto servicioDto);
    ServicioDto update(Long id, ServicioDto servicioDto);
    void delete(Long id);
}
