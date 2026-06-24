package com.mcmm.service.impl;

import com.mcmm.model.dto.responsableEvento.ResponsableEventoDto;
import com.mcmm.model.entity.ResponsableEvento;
import com.mcmm.model.entity.Evento;
import com.mcmm.model.entity.Cargo;
import com.mcmm.model.dao.ResponsableEventoDao;
import com.mcmm.model.dao.EventoDao;
import com.mcmm.model.dao.CargoDao;
import com.mcmm.service.IResponsableEvento;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Service
public class ResponsableEventoImpl implements IResponsableEvento {

    private final ResponsableEventoDao responsableEventoDao;
    private final EventoDao eventoDao;
    private final CargoDao cargoDao;
    private final ModelMapper modelMapper = new ModelMapper();

    public ResponsableEventoImpl(ResponsableEventoDao responsableEventoDao, EventoDao eventoDao, CargoDao cargoDao) {
        this.responsableEventoDao = responsableEventoDao;
        this.eventoDao = eventoDao;
        this.cargoDao = cargoDao;
    }

    private Long getCurrentIglesiaId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getDetails() instanceof Map) {
            Map<?, ?> details = (Map<?, ?>) authentication.getDetails();
            Object iglesiaIdObj = details.get("iglesiaId");
            if (iglesiaIdObj instanceof Long) {
                return (Long) iglesiaIdObj;
            }
        }
        return null;
    }

    @Override
    public List<ResponsableEventoDto> findAll() {
        Long iglesiaId = getCurrentIglesiaId();
        List<ResponsableEvento> responsables;
        if (iglesiaId != null) {
            responsables = responsableEventoDao.findByEventoIglesiaId(iglesiaId);
        } else {
            responsables = responsableEventoDao.findAll();
        }
        return responsables.stream()
                .map(responsable -> {
                    ResponsableEventoDto dto = modelMapper.map(responsable, ResponsableEventoDto.class);
                    if (responsable.getEvento() != null) {
                        dto.setEventoId(responsable.getEvento().getId());
                    }
                    if (responsable.getCargo() != null) {
                        dto.setCargoId(responsable.getCargo().getId());
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public ResponsableEventoDto findById(Long id) {
        ResponsableEvento responsable = responsableEventoDao.findById(id).orElse(null);
        if (responsable == null) return null;
        ResponsableEventoDto dto = modelMapper.map(responsable, ResponsableEventoDto.class);
        if (responsable.getEvento() != null) {
            dto.setEventoId(responsable.getEvento().getId());
        }
        if (responsable.getCargo() != null) {
            dto.setCargoId(responsable.getCargo().getId());
        }
        return dto;
    }

    @Override
    public ResponsableEventoDto create(ResponsableEventoDto responsableEventoDto) {
        ResponsableEvento responsable = modelMapper.map(responsableEventoDto, ResponsableEvento.class);
        if (responsableEventoDto.getEventoId() != null) {
            Evento evento = eventoDao.findById(responsableEventoDto.getEventoId()).orElse(null);
            responsable.setEvento(evento);
        }
        if (responsableEventoDto.getCargoId() != null) {
            Cargo cargo = cargoDao.findById(responsableEventoDto.getCargoId()).orElse(null);
            responsable.setCargo(cargo);
        }
        ResponsableEvento savedResponsable = responsableEventoDao.save(responsable);
        ResponsableEventoDto dto = modelMapper.map(savedResponsable, ResponsableEventoDto.class);
        if (savedResponsable.getEvento() != null) {
            dto.setEventoId(savedResponsable.getEvento().getId());
        }
        if (savedResponsable.getCargo() != null) {
            dto.setCargoId(savedResponsable.getCargo().getId());
        }
        return dto;
    }

    @Override
    public ResponsableEventoDto update(ResponsableEventoDto responsableEventoDto) {
        ResponsableEvento responsable = modelMapper.map(responsableEventoDto, ResponsableEvento.class);
        if (responsableEventoDto.getEventoId() != null) {
            Evento evento = eventoDao.findById(responsableEventoDto.getEventoId()).orElse(null);
            responsable.setEvento(evento);
        }
        if (responsableEventoDto.getCargoId() != null) {
            Cargo cargo = cargoDao.findById(responsableEventoDto.getCargoId()).orElse(null);
            responsable.setCargo(cargo);
        }
        ResponsableEvento updatedResponsable = responsableEventoDao.save(responsable);
        ResponsableEventoDto dto = modelMapper.map(updatedResponsable, ResponsableEventoDto.class);
        if (updatedResponsable.getEvento() != null) {
            dto.setEventoId(updatedResponsable.getEvento().getId());
        }
        if (updatedResponsable.getCargo() != null) {
            dto.setCargoId(updatedResponsable.getCargo().getId());
        }
        return dto;
    }

    @Override
    public void delete(Long id) {
        responsableEventoDao.deleteById(id);
    }

    @Override
    public void estado(Long id) {
        responsableEventoDao.toggleEstado(id);
    }
}