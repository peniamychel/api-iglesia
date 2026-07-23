package com.mcmm.service.impl;

import com.mcmm.model.dao.IglesiaDao;
import com.mcmm.model.dao.OfrendaDao;
import com.mcmm.model.dao.UsuarioDao;
import com.mcmm.model.dto.OfrendaDto;
import com.mcmm.model.entity.Ofrenda;
import com.mcmm.model.entity.Usuario;
import com.mcmm.service.IOfrenda;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class OfrendaImpl implements IOfrenda {

    private final OfrendaDao ofrendaDao;
    private final IglesiaDao iglesiaDao;
    private final UsuarioDao usuarioDao;
    private final ModelMapper modelMapper = new ModelMapper();

    public OfrendaImpl(OfrendaDao ofrendaDao, IglesiaDao iglesiaDao, UsuarioDao usuarioDao) {
        this.ofrendaDao = ofrendaDao;
        this.iglesiaDao = iglesiaDao;
        this.usuarioDao = usuarioDao;
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

    private OfrendaDto convertToDto(Ofrenda ofrenda) {
        if (ofrenda == null) return null;
        OfrendaDto dto = modelMapper.map(ofrenda, OfrendaDto.class);
        if (ofrenda.getIglesia() != null) {
            dto.setIglesiaId(ofrenda.getIglesia().getId());
            dto.setIglesiaNombre(ofrenda.getIglesia().getNombre());
        }
        if (ofrenda.getUsuarioTesorero() != null) {
            dto.setUsuarioTesoreroId(ofrenda.getUsuarioTesorero().getId());
            dto.setUsuarioTesoreroUsername(ofrenda.getUsuarioTesorero().getUsername());
        }
        if (ofrenda.getUsuarioModificacion() != null) {
            dto.setUsuarioModificacionUsername(ofrenda.getUsuarioModificacion().getUsername());
        }
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OfrendaDto> findAll() {
        Long iglesiaId = getCurrentIglesiaId();
        List<Ofrenda> ofrendas;
        if (iglesiaId != null) {
            ofrendas = ofrendaDao.findByIglesiaId(iglesiaId);
        } else {
            ofrendas = ofrendaDao.findAllActive();
        }
        return ofrendas.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OfrendaDto> findByIglesia(Long iglesiaId) {
        return ofrendaDao.findByIglesiaId(iglesiaId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OfrendaDto> findByIglesiaAndPeriod(Long iglesiaId, Date start, Date end) {
        return ofrendaDao.findByIglesiaIdAndFechaRecaudacionBetween(iglesiaId, start, end).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OfrendaDto> findByPeriod(Date start, Date end) {
        Long iglesiaId = getCurrentIglesiaId();
        List<Ofrenda> ofrendas;
        if (iglesiaId != null) {
            ofrendas = ofrendaDao.findByIglesiaIdAndFechaRecaudacionBetween(iglesiaId, start, end);
        } else {
            ofrendas = ofrendaDao.findByFechaRecaudacionBetween(start, end);
        }
        return ofrendas.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public OfrendaDto findById(Long id) {
        Ofrenda ofrenda = ofrendaDao.findById(id).orElse(null);
        return convertToDto(ofrenda);
    }

    @Override
    public OfrendaDto create(OfrendaDto ofrendaDto, Long usuarioId) {
        Ofrenda ofrenda = modelMapper.map(ofrendaDto, Ofrenda.class);
        
        Long iglesiaId = getCurrentIglesiaId();
        if (iglesiaId != null) {
            ofrenda.setIglesia(iglesiaDao.findById(iglesiaId).orElseThrow(() -> new RuntimeException("Iglesia no encontrada")));
        } else if (ofrendaDto.getIglesiaId() != null) {
            ofrenda.setIglesia(iglesiaDao.findById(ofrendaDto.getIglesiaId()).orElseThrow(() -> new RuntimeException("Iglesia no encontrada")));
        }

        if (ofrenda.getIglesia() == null) {
            throw new IllegalArgumentException("La iglesia es obligatoria");
        }

        if (usuarioId != null) {
            Usuario usuario = usuarioDao.findById(usuarioId).orElse(null);
            ofrenda.setUsuarioTesorero(usuario);
        }

        Ofrenda saved = ofrendaDao.save(ofrenda);
        return convertToDto(saved);
    }

    @Override
    public OfrendaDto update(OfrendaDto ofrendaDto, Long usuarioId) {
        Ofrenda existing = ofrendaDao.findById(ofrendaDto.getId())
                .orElseThrow(() -> new RuntimeException("Ofrenda no encontrada"));

        // Solo se editan monto, fecha y concepto. La iglesia y el tipo de movimiento
        // son inmutables tras el registro (no se toman del DTO del cliente).
        existing.setMonto(ofrendaDto.getMonto());
        existing.setFechaRecaudacion(ofrendaDto.getFechaRecaudacion());
        existing.setConceptoDetalle(ofrendaDto.getConceptoDetalle());

        // Auditoria: quien y cuando se realizo la edicion. Se fija explicitamente
        // (ademas de @PreUpdate) para que la respuesta inmediata ya lo refleje,
        // en vez de esperar al flush/commit de la transaccion.
        existing.setUpdatedAt(java.time.LocalDateTime.now());
        if (usuarioId != null) {
            existing.setUsuarioModificacion(usuarioDao.findById(usuarioId).orElse(null));
        }

        Ofrenda saved = ofrendaDao.save(existing);
        return convertToDto(saved);
    }

    @Override
    public void delete(Long id) {
        // Borrado logico: se conserva el registro con estado=false.
        Ofrenda existing = ofrendaDao.findById(id)
                .orElseThrow(() -> new RuntimeException("Ofrenda no encontrada"));
        existing.setEstado(false);
        ofrendaDao.save(existing);
    }

    @Override
    @Transactional(readOnly = true)
    public Double getSumByIglesiaAndTipoAndPeriod(Long iglesiaId, String tipo, Date start, Date end) {
        return ofrendaDao.sumMontoByIglesiaAndTipoAndPeriod(iglesiaId, tipo, start, end);
    }

    @Override
    @Transactional(readOnly = true)
    public Double getSumByTipoAndPeriod(String tipo, Date start, Date end) {
        Long iglesiaId = getCurrentIglesiaId();
        if (iglesiaId != null) {
            return ofrendaDao.sumMontoByIglesiaAndTipoAndPeriod(iglesiaId, tipo, start, end);
        } else {
            return ofrendaDao.sumMontoByTipoAndPeriod(tipo, start, end);
        }
    }
}
