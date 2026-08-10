package com.mcmm.service.impl;

import com.mcmm.exception.NotFoundExceptionResource;
import com.mcmm.model.dao.PlantillaCertificadoRepository;
import com.mcmm.model.dto.plantillaCertificado.PlantillaCertificadoDto;
import com.mcmm.model.entity.PlantillaCertificado;
import com.mcmm.service.PlantillaCertificadoService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlantillaCertificadoServiceImpl implements PlantillaCertificadoService {

    private final PlantillaCertificadoRepository plantillaCertificadoRepository;
    private final ModelMapper modelMapper;
    private final com.mcmm.service.FileStorageService fileStorageService;

    @Override
    @Transactional(readOnly = true)
    public List<PlantillaCertificado> findAll() {
        return plantillaCertificadoRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public PlantillaCertificado findById(Long id) {
        return plantillaCertificadoRepository.findById(id)
                .orElseThrow(() -> new NotFoundExceptionResource("PlantillaCertificado", "id", id));
    }

    @Override
    @Transactional
    public PlantillaCertificado save(PlantillaCertificadoDto plantillaCertificadoDto) {
        PlantillaCertificado plantilla = modelMapper.map(plantillaCertificadoDto, PlantillaCertificado.class);
        plantilla.setEstado(true);
        return plantillaCertificadoRepository.save(plantilla);
    }

    @Override
    @Transactional
    public PlantillaCertificado update(PlantillaCertificadoDto plantillaCertificadoDto, Long id) {
        PlantillaCertificado plantilla = findById(id);
        
        plantilla.setNombre(plantillaCertificadoDto.getNombre());
        plantilla.setConfiguracionJson(plantillaCertificadoDto.getConfiguracionJson());

        // Do not update estado from DTO generally, unless intended

        return plantillaCertificadoRepository.save(plantilla);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        PlantillaCertificado plantilla = findById(id);
        plantillaCertificadoRepository.delete(plantilla);
    }

    @Override
    @Transactional
    public PlantillaCertificado changeState(Long id) {
        PlantillaCertificado plantilla = findById(id);
        plantilla.setEstado(!plantilla.getEstado());
        return plantillaCertificadoRepository.save(plantilla);
    }

}
