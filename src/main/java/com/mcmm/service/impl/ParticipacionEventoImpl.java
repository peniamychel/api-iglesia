package com.mcmm.service.impl;

import com.mcmm.exception.NotFoundExceptionResource;
import com.mcmm.model.dto.participacionEvento.ParticipacionEventoDto;
import com.mcmm.model.entity.ParticipacionEvento;
import com.mcmm.model.entity.Certificado;
import com.mcmm.model.entity.Miembro;
import com.mcmm.model.entity.Evento;
import com.mcmm.model.entity.Usuario;
import com.mcmm.model.dao.ParticipacionEventoDao;
import com.mcmm.model.dao.CertificadoDao;
import com.mcmm.model.dao.MiembroDao;
import com.mcmm.model.dao.EventoDao;
import com.mcmm.model.dao.UsuarioDao;
import com.mcmm.service.IParticipacionEvento;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Service
@RequiredArgsConstructor
public class ParticipacionEventoImpl implements IParticipacionEvento {

    private final ParticipacionEventoDao participacionEventoDao;
    private final CertificadoDao certificadoDao;
    private final MiembroDao miembroDao;
    private final EventoDao eventoDao;
    private final UsuarioDao usuarioDao;
    private final ModelMapper modelMapper;

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
    @Transactional(readOnly = true)
    public List<ParticipacionEventoDto> findAll() {
        Long iglesiaId = getCurrentIglesiaId();
        List<ParticipacionEvento> participaciones;
        if (iglesiaId != null) {
            participaciones = participacionEventoDao.findByEventoIglesiaId(iglesiaId);
        } else {
            participaciones = StreamSupport.stream(participacionEventoDao.findAll().spliterator(), false)
                    .collect(Collectors.toList());
        }
        return participaciones.stream()
                .map(this::buildDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ParticipacionEventoDto findById(Long id) {
        ParticipacionEvento participacion = participacionEventoDao.findById(id)
                .orElseThrow(() -> new NotFoundExceptionResource("ParticipacionEvento", "id", id));
        return buildDto(participacion);
    }

    @Override
    @Transactional
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
        ParticipacionEvento saved = participacionEventoDao.save(participacion);
        return buildDto(saved);
    }

    @Override
    @Transactional
    public ParticipacionEventoDto update(ParticipacionEventoDto participacionEventoDto) {
        ParticipacionEvento participacion = participacionEventoDao.findById(participacionEventoDto.getId())
                .orElseThrow(() -> new NotFoundExceptionResource("ParticipacionEvento", "id", participacionEventoDto.getId()));
        participacion.setFecha(participacionEventoDto.getFecha());
        participacion.setEstado(participacionEventoDto.getEstado());
        participacion.setEntregado(participacionEventoDto.getEntregado());
        participacion.setFechaEntrega(participacionEventoDto.getFechaEntrega());
        if (participacionEventoDto.getCertificadoId() != null) {
            Certificado certificado = certificadoDao.findById(participacionEventoDto.getCertificadoId()).orElse(null);
            participacion.setCertificado(certificado);
        } else {
            participacion.setCertificado(null);
        }
        if (participacionEventoDto.getMiembroId() != null) {
            Miembro miembro = miembroDao.findById(participacionEventoDto.getMiembroId()).orElse(null);
            participacion.setMiembro(miembro);
        }
        if (participacionEventoDto.getEventoId() != null) {
            Evento evento = eventoDao.findById(participacionEventoDto.getEventoId()).orElse(null);
            participacion.setEvento(evento);
        }
        if (participacionEventoDto.getEntregadoPorId() != null) {
            Usuario usuario = usuarioDao.findById(participacionEventoDto.getEntregadoPorId()).orElse(null);
            participacion.setEntregadoPor(usuario);
        }
        ParticipacionEvento saved = participacionEventoDao.save(participacion);
        return buildDto(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        ParticipacionEvento participacion = participacionEventoDao.findById(id)
                .orElseThrow(() -> new NotFoundExceptionResource("ParticipacionEvento", "id", id));
        participacionEventoDao.delete(participacion);
    }

    @Override
    @Transactional
    public void estado(Long id) {
        participacionEventoDao.toggleEstado(id);
    }

    @Override
    @Transactional
    public void toggleEntregado(Long id, String username) {
        ParticipacionEvento participacion = participacionEventoDao.findById(id)
                .orElseThrow(() -> new NotFoundExceptionResource("ParticipacionEvento", "id", id));

        Usuario usuario = usuarioDao.findByUsername(username)
                .orElseThrow(() -> new NotFoundExceptionResource("Usuario", "username", username));

        if (participacion.getEntregado() == null || !participacion.getEntregado()) {
            participacion.setEntregado(true);
            participacion.setFechaEntrega(LocalDateTime.now());
            participacion.setEntregadoPor(usuario);
        } else {
            participacion.setEntregado(false);
            participacion.setFechaEntrega(null);
            participacion.setEntregadoPor(null);
        }

        participacionEventoDao.save(participacion);
    }

    private ParticipacionEventoDto buildDto(ParticipacionEvento participacion) {
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
        if (participacion.getEntregadoPor() != null) {
            dto.setEntregadoPorId(participacion.getEntregadoPor().getId());
        }
        return dto;
    }
}
