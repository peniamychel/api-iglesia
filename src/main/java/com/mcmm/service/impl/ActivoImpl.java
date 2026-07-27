package com.mcmm.service.impl;

import com.mcmm.exception.BadRequestException;
import com.mcmm.exception.NotFoundExceptionResource;
import com.mcmm.model.dao.ActivoDao;
import com.mcmm.model.dao.IglesiaDao;
import com.mcmm.model.dto.ActivoDto;
import com.mcmm.model.entity.Activo;
import com.mcmm.model.entity.Iglesia;
import com.mcmm.service.IActivo;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ActivoImpl implements IActivo {

    private final ActivoDao activoDao;
    private final IglesiaDao iglesiaDao;
    private final ModelMapper modelMapper;
    private final com.mcmm.service.FileStorageService fileStorageService;

    @org.springframework.beans.factory.annotation.Value("${file.upload-dir}")
    private String uploadDir;

    /**
     * Iglesia del token del usuario autenticado (null para el administrador global).
     * Mismo patrón que OfrendaImpl/EventoImpl: los roles de iglesia solo pueden
     * ver y gestionar los bienes de SU propia iglesia.
     */
    private Long getCurrentIglesiaId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getDetails() instanceof Map) {
            Map<?, ?> details = (Map<?, ?>) authentication.getDetails();
            Object iglesiaIdObj = details.get("iglesiaId");
            if (iglesiaIdObj instanceof Long) {
                return (Long) iglesiaIdObj;
            } else if (iglesiaIdObj instanceof Integer) {
                return ((Integer) iglesiaIdObj).longValue();
            }
        }
        return null;
    }

    /** Lanza 400 si el activo pertenece a otra iglesia distinta a la del token. */
    private void verificarPropiedad(Activo activo) {
        Long iglesiaId = getCurrentIglesiaId();
        if (iglesiaId != null && (activo.getIglesia() == null || !iglesiaId.equals(activo.getIglesia().getId()))) {
            throw new BadRequestException("No tiene acceso a los bienes de otra iglesia.");
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActivoDto> findAll() {
        Long iglesiaId = getCurrentIglesiaId();
        List<ActivoDto> list = new ArrayList<>();
        Iterable<Activo> activos = (iglesiaId != null)
                ? activoDao.findByIglesiaId(iglesiaId)
                : activoDao.findAll();
        activos.forEach(a -> list.add(convertToDto(a)));
        return list;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ActivoDto> findByIglesia(Long iglesiaId) {
        Long currentIglesiaId = getCurrentIglesiaId();
        if (currentIglesiaId != null && !currentIglesiaId.equals(iglesiaId)) {
            throw new BadRequestException("No tiene acceso a los bienes de otra iglesia.");
        }
        List<ActivoDto> list = new ArrayList<>();
        activoDao.findByIglesiaId(iglesiaId).forEach(a -> list.add(convertToDto(a)));
        return list;
    }

    @Override
    @Transactional(readOnly = true)
    public ActivoDto findById(Long id) {
        Activo activo = activoDao.findById(id)
                .orElseThrow(() -> new NotFoundExceptionResource("Activo", "id", id));
        verificarPropiedad(activo);
        return convertToDto(activo);
    }

    @Override
    @Transactional
    public ActivoDto save(ActivoDto activoDto) {
        // Los roles de iglesia registran siempre en SU iglesia, ignorando el id del payload.
        Long currentIglesiaId = getCurrentIglesiaId();
        Long iglesiaId = (currentIglesiaId != null) ? currentIglesiaId : activoDto.getIglesiaId();
        Iglesia iglesia = iglesiaDao.findById(iglesiaId)
                .orElseThrow(() -> new NotFoundExceptionResource("Iglesia", "id", iglesiaId));

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
        verificarPropiedad(exist);

        // Un rol de iglesia no puede reasignar el bien a otra iglesia.
        Long currentIglesiaId = getCurrentIglesiaId();
        Long iglesiaId = (currentIglesiaId != null) ? currentIglesiaId : activoDto.getIglesiaId();
        Iglesia iglesia = iglesiaDao.findById(iglesiaId)
                .orElseThrow(() -> new NotFoundExceptionResource("Iglesia", "id", iglesiaId));

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
        verificarPropiedad(exist);
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
        verificarPropiedad(exist);

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
        verificarPropiedad(exist);

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
