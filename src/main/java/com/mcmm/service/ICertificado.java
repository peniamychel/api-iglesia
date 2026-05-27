package com.mcmm.service;

import com.mcmm.model.dto.certificado.CertificadoDto;

import java.util.List;

public interface ICertificado {
    List<CertificadoDto> findAll();
    CertificadoDto findById(Long id);
    CertificadoDto create(CertificadoDto certificadoDto);
    CertificadoDto update(CertificadoDto certificadoDto);
    void delete(Long id);
    void estado(Long id);
}