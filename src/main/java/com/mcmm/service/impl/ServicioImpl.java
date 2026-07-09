package com.mcmm.service.impl;

import com.mcmm.exception.NotFoundExceptionResource;
import com.mcmm.model.dao.ServicioDao;
import com.mcmm.model.dto.AccionDto;
import com.mcmm.model.dto.ServicioDto;
import com.mcmm.model.entity.Servicio;
import com.mcmm.service.IServicio;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ServicioImpl implements IServicio {

    private final ServicioDao servicioDao;
    private final ModelMapper modelMapper;

    public ServicioImpl(ServicioDao servicioDao) {
        this.servicioDao = servicioDao;
        this.modelMapper = new ModelMapper();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ServicioDto> findAll() {
        return servicioDao.findByActivoTrueOrderByOrdenAsc().stream()
                .map(s -> {
                    ServicioDto dto = modelMapper.map(s, ServicioDto.class);
                    if (s.getAcciones() != null) {
                        dto.setAcciones(s.getAcciones().stream().map(a -> {
                            AccionDto aDto = modelMapper.map(a, AccionDto.class);
                            aDto.setAuthorityCode(a.getAuthorityCode());
                            aDto.setServicioCodigo(s.getCodigo());
                            return aDto;
                        }).collect(Collectors.toSet()));
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ServicioDto findById(Long id) {
        Servicio s = servicioDao.findById(id)
                .orElseThrow(() -> new NotFoundExceptionResource("Servicio", "id", id));
        ServicioDto dto = modelMapper.map(s, ServicioDto.class);
        if (s.getAcciones() != null) {
            dto.setAcciones(s.getAcciones().stream().map(a -> {
                AccionDto aDto = modelMapper.map(a, AccionDto.class);
                aDto.setAuthorityCode(a.getAuthorityCode());
                aDto.setServicioCodigo(s.getCodigo());
                return aDto;
            }).collect(Collectors.toSet()));
        }
        return dto;
    }

    @Override
    public ServicioDto create(ServicioDto servicioDto) {
        Servicio s = modelMapper.map(servicioDto, Servicio.class);
        Servicio saved = servicioDao.save(s);
        return modelMapper.map(saved, ServicioDto.class);
    }

    @Override
    public ServicioDto update(Long id, ServicioDto servicioDto) {
        Servicio s = servicioDao.findById(id)
                .orElseThrow(() -> new NotFoundExceptionResource("Servicio", "id", id));
        s.setNombre(servicioDto.getNombre());
        s.setCodigo(servicioDto.getCodigo());
        s.setDescripcion(servicioDto.getDescripcion());
        s.setIcono(servicioDto.getIcono());
        s.setRuta(servicioDto.getRuta());
        s.setOrden(servicioDto.getOrden());
        if (servicioDto.getActivo() != null) {
            s.setActivo(servicioDto.getActivo());
        }
        Servicio updated = servicioDao.save(s);
        return modelMapper.map(updated, ServicioDto.class);
    }

    @Override
    public void delete(Long id) {
        Servicio s = servicioDao.findById(id)
                .orElseThrow(() -> new NotFoundExceptionResource("Servicio", "id", id));
        servicioDao.delete(s);
    }
}
