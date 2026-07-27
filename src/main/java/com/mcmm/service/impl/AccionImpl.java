package com.mcmm.service.impl;

import com.mcmm.exception.NotFoundExceptionResource;
import com.mcmm.model.dao.AccionDao;
import com.mcmm.model.dao.ServicioDao;
import com.mcmm.model.dto.AccionDto;
import com.mcmm.model.entity.Accion;
import com.mcmm.model.entity.Servicio;
import com.mcmm.service.IAccion;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AccionImpl implements IAccion {

    private final AccionDao accionDao;
    private final ServicioDao servicioDao;
    private final ModelMapper modelMapper;

    public AccionImpl(AccionDao accionDao, ServicioDao servicioDao) {
        this.accionDao = accionDao;
        this.servicioDao = servicioDao;
        this.modelMapper = new ModelMapper();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccionDto> findAll() {
        return accionDao.findAll().stream()
                .map(a -> {
                    AccionDto dto = modelMapper.map(a, AccionDto.class);
                    dto.setAuthorityCode(a.getAuthorityCode());
                    if (a.getServicio() != null) {
                        dto.setServicioId(a.getServicio().getId());
                        dto.setServicioCodigo(a.getServicio().getCodigo());
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccionDto> findByServicioId(Long servicioId) {
        return accionDao.findByServicioIdAndActivoTrue(servicioId).stream()
                .map(a -> {
                    AccionDto dto = modelMapper.map(a, AccionDto.class);
                    dto.setAuthorityCode(a.getAuthorityCode());
                    if (a.getServicio() != null) {
                        dto.setServicioId(a.getServicio().getId());
                        dto.setServicioCodigo(a.getServicio().getCodigo());
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AccionDto findById(Long id) {
        Accion a = accionDao.findById(id)
                .orElseThrow(() -> new NotFoundExceptionResource("Accion", "id", id));
        AccionDto dto = modelMapper.map(a, AccionDto.class);
        dto.setAuthorityCode(a.getAuthorityCode());
        if (a.getServicio() != null) {
            dto.setServicioId(a.getServicio().getId());
            dto.setServicioCodigo(a.getServicio().getCodigo());
        }
        return dto;
    }

    @Override
    public AccionDto create(AccionDto accionDto) {
        Servicio s = servicioDao.findById(accionDto.getServicioId())
                .orElseThrow(() -> new NotFoundExceptionResource("Servicio", "id", accionDto.getServicioId()));
        Accion a = modelMapper.map(accionDto, Accion.class);
        a.setServicio(s);
        Accion saved = accionDao.save(a);
        AccionDto dto = modelMapper.map(saved, AccionDto.class);
        dto.setAuthorityCode(saved.getAuthorityCode());
        dto.setServicioId(s.getId());
        dto.setServicioCodigo(s.getCodigo());
        return dto;
    }

    @Override
    public AccionDto update(Long id, AccionDto accionDto) {
        Accion a = accionDao.findById(id)
                .orElseThrow(() -> new NotFoundExceptionResource("Accion", "id", id));
        a.setNombre(accionDto.getNombre());
        a.setCodigo(accionDto.getCodigo());
        a.setDescripcion(accionDto.getDescripcion());
        if (accionDto.getActivo() != null) {
            a.setActivo(accionDto.getActivo());
        }
        Accion updated = accionDao.save(a);
        AccionDto dto = modelMapper.map(updated, AccionDto.class);
        dto.setAuthorityCode(updated.getAuthorityCode());
        if (updated.getServicio() != null) {
            dto.setServicioId(updated.getServicio().getId());
            dto.setServicioCodigo(updated.getServicio().getCodigo());
        }
        return dto;
    }

    @Override
    public void delete(Long id) {
        Accion a = accionDao.findById(id)
                .orElseThrow(() -> new NotFoundExceptionResource("Accion", "id", id));
        accionDao.delete(a);
    }
}
