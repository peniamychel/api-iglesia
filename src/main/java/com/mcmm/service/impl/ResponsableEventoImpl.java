package com.mcmm.service.impl;

import com.mcmm.model.dto.responsableEvento.ResponsableEventoDto;
import com.mcmm.model.entity.ResponsableEvento;
import com.mcmm.model.entity.Evento;
import com.mcmm.model.entity.Cargo;
import com.mcmm.model.dao.ResponsableEventoDao;
import com.mcmm.model.dao.EventoDao;
import com.mcmm.model.dao.CargoDao;
import com.mcmm.exception.NotFoundExceptionResource;
import com.mcmm.service.IResponsableEvento;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Service
@Transactional
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
            } else if (iglesiaIdObj instanceof Integer) {
                return ((Integer) iglesiaIdObj).longValue();
            }
        }
        return null;
    }

    @Override
    @Transactional(readOnly = true)
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
    @Transactional(readOnly = true)
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
        // Se arma a mano en vez de con ModelMapper: el DTO lleva nombreCompleto y
        // nombreCargo (solo de lectura) y el mapeo implícito los toma como
        // candidatos para evento.nombre, lo que aborta con una ambiguedad.
        ResponsableEvento responsable = new ResponsableEvento();
        responsable.setEstado(responsableEventoDto.getEstado());
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
        // Se carga la fila existente y se copian solo los campos editables (mismo
        // motivo que en create para no usar ModelMapper hacia la entidad).
        ResponsableEvento responsable = responsableEventoDao.findById(responsableEventoDto.getId())
                .orElseThrow(() -> new NotFoundExceptionResource("ResponsableEvento", "id", responsableEventoDto.getId()));
        if (responsableEventoDto.getEstado() != null) {
            responsable.setEstado(responsableEventoDto.getEstado());
        }
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
    @Transactional(readOnly = true)
    public List<ResponsableEventoDto> findByEventoId(Long eventoId) {
        List<ResponsableEvento> responsables = responsableEventoDao.findByEventoIdWithRelations(eventoId);
        return responsables.stream()
                .map(responsable -> {
                    ResponsableEventoDto dto = modelMapper.map(responsable, ResponsableEventoDto.class);
                    if (responsable.getEvento() != null) {
                        dto.setEventoId(responsable.getEvento().getId());
                    }
                    if (responsable.getCargo() != null) {
                        dto.setCargoId(responsable.getCargo().getId());
                        if (responsable.getCargo().getMiembro() != null) {
                            dto.setNombreCompleto(responsable.getCargo().getMiembro().getNombre() + " " + responsable.getCargo().getMiembro().getApellido());
                        }
                        if (responsable.getCargo().getRolCargo() != null) {
                            dto.setNombreCargo(responsable.getCargo().getRolCargo().getNombre());
                        }
                    }
                    return dto;
                })
                .collect(Collectors.toList());
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