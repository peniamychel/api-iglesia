package com.mcmm.service.impl;

import com.mcmm.exception.NotFoundExceptionResource;
import com.mcmm.model.dao.ActivoDao;
import com.mcmm.model.dao.IglesiaDao;
import com.mcmm.model.dto.ActivoDto;
import com.mcmm.model.entity.Activo;
import com.mcmm.model.entity.Iglesia;
import com.mcmm.service.IActivo;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivoImpl implements IActivo {

    private final ActivoDao activoDao;
    private final IglesiaDao iglesiaDao;
    private final ModelMapper modelMapper;
    private final com.mcmm.service.FileStorageService fileStorageService;

    @org.springframework.beans.factory.annotation.Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    @Transactional(readOnly = true)
    public List<ActivoDto> findAll() {
        List<ActivoDto> list = new ArrayList<>();
        activoDao.findAll().forEach(a -> list.add(convertToDto(a)));
        return list;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActivoDto> findByIglesia(Long iglesiaId) {
        List<ActivoDto> list = new ArrayList<>();
        activoDao.findByIglesiaId(iglesiaId).forEach(a -> list.add(convertToDto(a)));
        return list;
    }

    @Override
    @Transactional(readOnly = true)
    public ActivoDto findById(Long id) {
        Activo activo = activoDao.findById(id)
                .orElseThrow(() -> new NotFoundExceptionResource("Activo", "id", id));
        return convertToDto(activo);
    }

    @Override
    @Transactional
    public ActivoDto save(ActivoDto activoDto) {
        Iglesia iglesia = iglesiaDao.findById(activoDto.getIglesiaId())
                .orElseThrow(() -> new NotFoundExceptionResource("Iglesia", "id", activoDto.getIglesiaId()));

        Activo activo = modelMapper.map(activoDto, Activo.class);
        activo.setIglesia(iglesia);
        Activo saved = activoDao.save(activo);
        return convertToDto(saved);
    }

    @Override
    @Transactional
    public ActivoDto update(ActivoDto activoDto) {
        Activo exist = activoDao.findById(activoDto.getId())
                .orElseThrow(() -> new NotFoundExceptionResource("Activo", "id", activoDto.getId()));

        Iglesia iglesia = iglesiaDao.findById(activoDto.getIglesiaId())
                .orElseThrow(() -> new NotFoundExceptionResource("Iglesia", "id", activoDto.getIglesiaId()));

        exist.setNombre(activoDto.getNombre());
        exist.setDescripcion(activoDto.getDescripcion());
        exist.setCantidad(activoDto.getCantidad());
        exist.setEstadoConservacion(activoDto.getEstadoConservacion());
        exist.setValorEstimado(activoDto.getValorEstimado());
        exist.setFechaAdquisicion(activoDto.getFechaAdquisicion());
        exist.setCodigo(activoDto.getCodigo());
        exist.setUriFoto(activoDto.getUriFoto());
        exist.setIglesia(iglesia);

        Activo updated = activoDao.save(exist);
        return convertToDto(updated);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Activo exist = activoDao.findById(id)
                .orElseThrow(() -> new NotFoundExceptionResource("Activo", "id", id));
        activoDao.delete(exist);
    }

    private ActivoDto convertToDto(Activo activo) {
        ActivoDto dto = modelMapper.map(activo, ActivoDto.class);
        if (activo.getIglesia() != null) {
            dto.setIglesiaId(activo.getIglesia().getId());
            dto.setIglesiaNombre(activo.getIglesia().getNombre());
        }
        if (activo.getUriFoto() != null && !activo.getUriFoto().isBlank()) {
            try {
                String fileUrl = org.springframework.web.servlet.support.ServletUriComponentsBuilder
                        .fromCurrentContextPath()
                        .path(uploadDir)
                        .path("/")
                        .path(activo.getUriFoto())
                        .toUriString();
                dto.setUriFoto(fileUrl);
            } catch (Exception e) {
                // Dejar ruta relativa si no hay contexto de request
            }
        }
        return dto;
    }

    @Override
    @Transactional
    public String uploadPhoto(Long id, org.springframework.web.multipart.MultipartFile file) throws java.io.IOException {
        Activo exist = activoDao.findById(id)
                .orElseThrow(() -> new NotFoundExceptionResource("Activo", "id", id));
        
        if (exist.getUriFoto() != null) {
            try {
                fileStorageService.deleteFile(exist.getUriFoto());
            } catch (Exception e) {
                // Ignorar error al borrar si no existía el archivo físico
            }
        }

        String fileName = fileStorageService.storeFile(file, "activo", "activos");
        String uriFoto = "activos/" + fileName;
        exist.setUriFoto(uriFoto);
        activoDao.save(exist);

        String fileUrl = org.springframework.web.servlet.support.ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path(uploadDir)
                .path("/")
                .path(uriFoto)
                .toUriString();
        return fileUrl;
    }

    @Override
    @Transactional
    public void deletePhoto(Long id) {
        Activo exist = activoDao.findById(id)
                .orElseThrow(() -> new NotFoundExceptionResource("Activo", "id", id));
        
        if (exist.getUriFoto() != null) {
            try {
                fileStorageService.deleteFile(exist.getUriFoto());
            } catch (Exception e) {
                // Ignorar error al borrar si no existía el archivo físico
            }
            exist.setUriFoto(null);
            activoDao.save(exist);
        }
    }
}
