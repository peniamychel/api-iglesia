package com.mcmm.service.impl;

import com.mcmm.model.dto.participacionEvento.ParticipacionEventoDto;
import com.mcmm.model.entity.ParticipacionEvento;
import com.mcmm.model.entity.Certificado;
import com.mcmm.model.entity.Miembro;
import com.mcmm.model.entity.Evento;
import com.mcmm.model.dao.ParticipacionEventoDao;
import com.mcmm.model.dao.CertificadoDao;
import com.mcmm.model.dao.MiembroDao;
import com.mcmm.model.dao.EventoDao;
import com.mcmm.service.IParticipacionEvento;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ParticipacionEventoImpl implements IParticipacionEvento {

    private final ParticipacionEventoDao participacionEventoDao;
    private final CertificadoDao certificadoDao;
    private final MiembroDao miembroDao;
    private final EventoDao eventoDao;
    private final ModelMapper modelMapper = new ModelMapper();

    public ParticipacionEventoImpl(ParticipacionEventoDao participacionEventoDao,
                                   CertificadoDao certificadoDao,
                                   MiembroDao miembroDao,
                                   EventoDao eventoDao) {
        this.participacionEventoDao = participacionEventoDao;
        this.certificadoDao = certificadoDao;
        this.miembroDao = miembroDao;
        this.eventoDao = eventoDao;
    }

    @Override
    public List<ParticipacionEventoDto> findAll() {
        List<ParticipacionEvento> participaciones = participacionEventoDao.findAll();
        return participaciones.stream()
                .map(participacion -> {
                    ParticipacionEventoDto dto = modelMapper.map(participacion, ParticipacionEventoDto.class);
                    if (participacion.getCertificado() != null) {
                        dto.setCertificadoId(participacion.getCertificado().getId());
                    }
                    if (participacion.getMiembro() != null) {
                        dto.setMiembroId(participacion.getMiembro().getId());
                    }
                    if (participacion.getEvento() != null) {
                        dto.setEventoId(participacion.getEvento().getId());
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public ParticipacionEventoDto findById(Long id) {
        ParticipacionEvento participacion = participacionEventoDao.findById(id).orElse(null);
        if (participacion == null) return null;
        ParticipacionEventoDto dto = modelMapper.map(participacion, ParticipacionEventoDto.class);
        if (participacion.getCertificado() != null) {
            dto.setCertificadoId(participacion.getCertificado().getId());
        }
        if (participacion.getMiembro() != null) {
            dto.setMiembroId(participacion.getMiembro().getId());
        }
        if (participacion.getEvento() != null) {
            dto.setEventoId(participacion.getEvento().getId());
        }
        return dto;
    }

    @Override
    public ParticipacionEventoDto create(ParticipacionEventoDto participacionEventoDto) {
        ParticipacionEvento participacion = modelMapper.map(participacionEventoDto, ParticipacionEvento.class);
        if (participacionEventoDto.getCertificadoId() != null) {
            Certificado certificado = certificadoDao.findById(participacionEventoDto.getCertificadoId()).orElse(null);
            participacion.setCertificado(certificado);
        }
        if (participacionEventoDto.getMiembroId() != null) {
            Miembro miembro = miembroDao.findById(participacionEventoDto.getMiembroId()).orElse(null);
            participacion.setMiembro(miembro);
        }
        if (participacionEventoDto.getEventoId() != null) {
            Evento evento = eventoDao.findById(participacionEventoDto.getEventoId()).orElse(null);
            participacion.setEvento(evento);
        }
        ParticipacionEvento savedParticipacion = participacionEventoDao.save(participacion);
        ParticipacionEventoDto dto = modelMapper.map(savedParticipacion, ParticipacionEventoDto.class);
        if (savedParticipacion.getCertificado() != null) {
            dto.setCertificadoId(savedParticipacion.getCertificado().getId());
        }
        if (savedParticipacion.getMiembro() != null) {
            dto.setMiembroId(savedParticipacion.getMiembro().getId());
        }
        if (savedParticipacion.getEvento() != null) {
            dto.setEventoId(savedParticipacion.getEvento().getId());
        }
        return dto;
    }

    @Override
    public ParticipacionEventoDto update(ParticipacionEventoDto participacionEventoDto) {
        ParticipacionEvento participacion = modelMapper.map(participacionEventoDto, ParticipacionEvento.class);
        if (participacionEventoDto.getCertificadoId() != null) {
            Certificado certificado = certificadoDao.findById(participacionEventoDto.getCertificadoId()).orElse(null);
            participacion.setCertificado(certificado);
        }
        if (participacionEventoDto.getMiembroId() != null) {
            Miembro miembro = miembroDao.findById(participacionEventoDto.getMiembroId()).orElse(null);
            participacion.setMiembro(miembro);
        }
        if (participacionEventoDto.getEventoId() != null) {
            Evento evento = eventoDao.findById(participacionEventoDto.getEventoId()).orElse(null);
            participacion.setEvento(evento);
        }
        ParticipacionEvento updatedParticipacion = participacionEventoDao.save(participacion);
        ParticipacionEventoDto dto = modelMapper.map(updatedParticipacion, ParticipacionEventoDto.class);
        if (updatedParticipacion.getCertificado() != null) {
            dto.setCertificadoId(updatedParticipacion.getCertificado().getId());
        }
        if (updatedParticipacion.getMiembro() != null) {
            dto.setMiembroId(updatedParticipacion.getMiembro().getId());
        }
        if (updatedParticipacion.getEvento() != null) {
            dto.setEventoId(updatedParticipacion.getEvento().getId());
        }
        return dto;
    }

    @Override
    public void delete(Long id) {
        participacionEventoDao.deleteById(id);
    }

    @Override
    public void estado(Long id) {
        participacionEventoDao.toggleEstado(id);
    }
}