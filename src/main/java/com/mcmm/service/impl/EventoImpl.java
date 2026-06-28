package com.mcmm.service.impl;

import com.mcmm.model.dto.evento.EventoDto;
import com.mcmm.model.entity.Evento;
import com.mcmm.model.entity.TipoEvento;
import com.mcmm.model.dao.EventoDao;
import com.mcmm.model.dao.TipoEventoDao;
import com.mcmm.model.dao.IglesiaDao;
import com.mcmm.service.IEvento;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EventoImpl implements IEvento {

    private final EventoDao eventoDao;
    private final TipoEventoDao tipoEventoDao;
    private final IglesiaDao iglesiaDao;
    private final ModelMapper modelMapper = new ModelMapper();

    public EventoImpl(EventoDao eventoDao, TipoEventoDao tipoEventoDao, IglesiaDao iglesiaDao) {
        this.eventoDao = eventoDao;
        this.tipoEventoDao = tipoEventoDao;
        this.iglesiaDao = iglesiaDao;
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
    public List<EventoDto> findAll() {
        Long iglesiaId = getCurrentIglesiaId();
        List<Evento> eventos;
        if (iglesiaId != null) {
            eventos = eventoDao.findByIglesiaId(iglesiaId);
        } else {
            eventos = eventoDao.findAll();
        }
        return eventos.stream()
                .map(evento -> {
                    EventoDto dto = modelMapper.map(evento, EventoDto.class);
                    if (evento.getTipoEvento() != null) {
                        dto.setTipoEventoId(evento.getTipoEvento().getId());
                    }
                    if (evento.getIglesia() != null) {
                        dto.setIglesiaId(evento.getIglesia().getId());
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public EventoDto findById(Long id) {
        Evento evento = eventoDao.findById(id).orElse(null);
        if (evento == null) return null;
        EventoDto dto = modelMapper.map(evento, EventoDto.class);
        if (evento.getTipoEvento() != null) {
            dto.setTipoEventoId(evento.getTipoEvento().getId());
        }
        if (evento.getIglesia() != null) {
            dto.setIglesiaId(evento.getIglesia().getId());
        }
        return dto;
    }

    @Override
    public EventoDto create(EventoDto eventoDto) {
        Evento evento = modelMapper.map(eventoDto, Evento.class);
        if (eventoDto.getTipoEventoId() != null) {
            TipoEvento tipoEvento = tipoEventoDao.findById(eventoDto.getTipoEventoId()).orElse(null);
            evento.setTipoEvento(tipoEvento);
        }
        
        Long iglesiaId = getCurrentIglesiaId();
        if (iglesiaId != null) {
            evento.setIglesia(iglesiaDao.findById(iglesiaId).orElse(null));
        } else if (eventoDto.getIglesiaId() != null) {
            evento.setIglesia(iglesiaDao.findById(eventoDto.getIglesiaId()).orElse(null));
        }

        Evento savedEvento = eventoDao.save(evento);
        EventoDto dto = modelMapper.map(savedEvento, EventoDto.class);
        if (savedEvento.getTipoEvento() != null) {
            dto.setTipoEventoId(savedEvento.getTipoEvento().getId());
        }
        if (savedEvento.getIglesia() != null) {
            dto.setIglesiaId(savedEvento.getIglesia().getId());
        }
        return dto;
    }

    @Override
    public EventoDto update(EventoDto eventoDto) {
        Evento evento = modelMapper.map(eventoDto, Evento.class);
        if (eventoDto.getTipoEventoId() != null) {
            TipoEvento tipoEvento = tipoEventoDao.findById(eventoDto.getTipoEventoId()).orElse(null);
            evento.setTipoEvento(tipoEvento);
        }
        
        if (eventoDto.getIglesiaId() != null) {
            evento.setIglesia(iglesiaDao.findById(eventoDto.getIglesiaId()).orElse(null));
        } else {
            Long iglesiaId = getCurrentIglesiaId();
            if (iglesiaId != null) {
                evento.setIglesia(iglesiaDao.findById(iglesiaId).orElse(null));
            }
        }

        Evento updatedEvento = eventoDao.save(evento);
        EventoDto dto = modelMapper.map(updatedEvento, EventoDto.class);
        if (updatedEvento.getTipoEvento() != null) {
            dto.setTipoEventoId(updatedEvento.getTipoEvento().getId());
        }
        if (updatedEvento.getIglesia() != null) {
            dto.setIglesiaId(updatedEvento.getIglesia().getId());
        }
        return dto;
    }

    @Override
    public void delete(Long id) {
        eventoDao.deleteById(id);
    }

    @Override
    public void estado(Long id) {
        eventoDao.toggleEstado(id);
    }

    @Override
    public void cloneYearEvents(int fromYear, int toYear) {
        Calendar cal = Calendar.getInstance();
        cal.set(fromYear, Calendar.JANUARY, 1, 0, 0, 0);
        Date start = cal.getTime();
        cal.set(fromYear, Calendar.DECEMBER, 31, 23, 59, 59);
        Date end = cal.getTime();

        List<Evento> eventsFromYear = eventoDao.findByFechaInicioBetween(start, end);
        int yearDiff = toYear - fromYear;

        for (Evento original : eventsFromYear) {
            if (Boolean.TRUE.equals(original.getMostrarEnCalendario()) || "GENERAL".equalsIgnoreCase(original.getAlcance())) {
                Evento cloned = Evento.builder()
                        .nombre(original.getNombre())
                        .motivo(original.getMotivo())
                        .ubicacion(original.getUbicacion())
                        .tipoEvento(original.getTipoEvento())
                        .iglesia(original.getIglesia())
                        .alcance(original.getAlcance())
                        .mostrarEnCalendario(original.getMostrarEnCalendario())
                        .estado(true)
                        .build();

                if (original.getFechaInicio() != null) {
                    cal.setTime(original.getFechaInicio());
                    cal.add(Calendar.YEAR, yearDiff);
                    cloned.setFechaInicio(cal.getTime());
                }
                if (original.getFechaFin() != null) {
                    cal.setTime(original.getFechaFin());
                    cal.add(Calendar.YEAR, yearDiff);
                    cloned.setFechaFin(cal.getTime());
                }

                eventoDao.save(cloned);
            }
        }
    }
}