package com.mcmm.service;

import com.mcmm.model.dto.OfrendaDto;

import java.util.Date;
import java.util.List;

public interface IOfrenda {
    List<OfrendaDto> findAll();
    List<OfrendaDto> findByIglesia(Long iglesiaId);
    List<OfrendaDto> findByIglesiaAndPeriod(Long iglesiaId, Date start, Date end);
    List<OfrendaDto> findByPeriod(Date start, Date end);
    OfrendaDto findById(Long id);
    OfrendaDto create(OfrendaDto ofrendaDto, Long usuarioId);
    OfrendaDto update(OfrendaDto ofrendaDto);
    void delete(Long id);
    Double getSumByIglesiaAndTipoAndPeriod(Long iglesiaId, String tipo, Date start, Date end);
    Double getSumByTipoAndPeriod(String tipo, Date start, Date end);
}
