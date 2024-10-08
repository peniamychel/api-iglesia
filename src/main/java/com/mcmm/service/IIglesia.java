package com.mcmm.service;

import com.mcmm.model.dto.IglesiaDto;

import java.util.List;

public interface IIglesia {

    public Iterable<IglesiaDto> findAll();

    public IglesiaDto findById(Long id);

    public IglesiaDto save(IglesiaDto iglesiaDto);

    public void delete(IglesiaDto iglesiaDto);

    public IglesiaDto update(Long id, IglesiaDto iglesiaDto);

}
