package com.mcmm.service;

import com.mcmm.model.dto.certificado.CertificadoDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ICertificado {
    List<CertificadoDto> findAll();
    CertificadoDto findById(Long id);
    CertificadoDto create(CertificadoDto certificadoDto);
    CertificadoDto update(CertificadoDto certificadoDto);
    void delete(Long id);
    void estado(Long id);
    String uploadProfilePhoto(Long id, MultipartFile file) throws IOException;
    void deleteProfilePhoto(Long id);
}