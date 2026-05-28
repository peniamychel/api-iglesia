package com.mcmm.service;

import com.mcmm.model.dto.iglesiaDto.IglesiaDto;

import java.util.List;

public interface IIglesia {
    List<IglesiaDto> findAll();
    IglesiaDto findById(Long id);
    IglesiaDto save(IglesiaDto iglesiaDto);
    void delete(Long id);
    IglesiaDto update(Long id, IglesiaDto iglesiaDto);
    IglesiaDto estado(Long id);
    IglesiaDto buscarNombreIglesia(String nameIglesia);
    IglesiaDto buscarNombreIglesiaExceptoId(Long id, String nameIglesia);
    List<IglesiaDto> findByEstadoTrue();
    IglesiaDto findByNombreAndIdNot(String nameIglesia, Long id);
}
