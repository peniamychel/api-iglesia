package com.mcmm.service;

import com.mcmm.model.dto.plantillaCertificado.PlantillaCertificadoDto;
import com.mcmm.model.entity.PlantillaCertificado;

import java.util.List;

public interface PlantillaCertificadoService {

    List<PlantillaCertificado> findAll();

    PlantillaCertificado findById(Long id);

    PlantillaCertificado save(PlantillaCertificadoDto plantillaCertificadoDto);

    PlantillaCertificado update(PlantillaCertificadoDto plantillaCertificadoDto, Long id);

    void delete(Long id);

    PlantillaCertificado changeState(Long id);

    String uploadLogo(Long id, org.springframework.web.multipart.MultipartFile file) throws java.io.IOException;

    String uploadMarcaAgua(Long id, org.springframework.web.multipart.MultipartFile file) throws java.io.IOException;

    String uploadFirma(Long id, org.springframework.web.multipart.MultipartFile file) throws java.io.IOException;
}
