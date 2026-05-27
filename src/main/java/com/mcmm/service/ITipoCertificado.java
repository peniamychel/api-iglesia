package com.mcmm.service;

import com.mcmm.model.dto.tipoCertificado.TipoCertificadoDto;

import java.util.List;

public interface ITipoCertificado {
    List<TipoCertificadoDto> findAll();
    TipoCertificadoDto findById(Long id);
    TipoCertificadoDto create(TipoCertificadoDto tipoCertificadoDto);
    TipoCertificadoDto update(TipoCertificadoDto tipoCertificadoDto);
    void delete(Long id);
    void estado(Long id);
}