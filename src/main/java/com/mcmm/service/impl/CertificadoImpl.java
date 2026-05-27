package com.mcmm.service.impl;

import com.mcmm.model.dto.certificado.CertificadoDto;
import com.mcmm.model.entity.Certificado;
import com.mcmm.model.entity.Evento;
import com.mcmm.model.entity.TipoCertificado;
import com.mcmm.model.dao.CertificadoDao;
import com.mcmm.model.dao.EventoDao;
import com.mcmm.model.dao.TipoCertificadoDao;
import com.mcmm.service.ICertificado;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CertificadoImpl implements ICertificado {

    private final CertificadoDao certificadoDao;
    private final EventoDao eventoDao;
    private final TipoCertificadoDao tipoCertificadoDao;
    private final ModelMapper modelMapper = new ModelMapper();

    public CertificadoImpl(CertificadoDao certificadoDao, EventoDao eventoDao, TipoCertificadoDao tipoCertificadoDao) {
        this.certificadoDao = certificadoDao;
        this.eventoDao = eventoDao;
        this.tipoCertificadoDao = tipoCertificadoDao;
    }

    @Override
    public List<CertificadoDto> findAll() {
        List<Certificado> certificados = certificadoDao.findAll();
        return certificados.stream()
                .map(certificado -> {
                    CertificadoDto dto = modelMapper.map(certificado, CertificadoDto.class);
                    if (certificado.getEvento() != null) {
                        dto.setEventoId(certificado.getEvento().getId());
                    }
                    if (certificado.getTipoCertificado() != null) {
                        dto.setTipoCertificadoId(certificado.getTipoCertificado().getId());
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public CertificadoDto findById(Long id) {
        Certificado certificado = certificadoDao.findById(id).orElse(null);
        if (certificado == null) return null;
        CertificadoDto dto = modelMapper.map(certificado, CertificadoDto.class);
        if (certificado.getEvento() != null) {
            dto.setEventoId(certificado.getEvento().getId());
        }
        if (certificado.getTipoCertificado() != null) {
            dto.setTipoCertificadoId(certificado.getTipoCertificado().getId());
        }
        return dto;
    }

    @Override
    public CertificadoDto create(CertificadoDto certificadoDto) {
        Certificado certificado = modelMapper.map(certificadoDto, Certificado.class);
        if (certificadoDto.getEventoId() != null) {
            Evento evento = eventoDao.findById(certificadoDto.getEventoId()).orElse(null);
            certificado.setEvento(evento);
        }
        if (certificadoDto.getTipoCertificadoId() != null) {
            TipoCertificado tipoCertificado = tipoCertificadoDao.findById(certificadoDto.getTipoCertificadoId()).orElse(null);
            certificado.setTipoCertificado(tipoCertificado);
        }
        Certificado savedCertificado = certificadoDao.save(certificado);
        CertificadoDto dto = modelMapper.map(savedCertificado, CertificadoDto.class);
        if (savedCertificado.getEvento() != null) {
            dto.setEventoId(savedCertificado.getEvento().getId());
        }
        if (savedCertificado.getTipoCertificado() != null) {
            dto.setTipoCertificadoId(savedCertificado.getTipoCertificado().getId());
        }
        return dto;
    }

    @Override
    public CertificadoDto update(CertificadoDto certificadoDto) {
        Certificado certificado = modelMapper.map(certificadoDto, Certificado.class);
        if (certificadoDto.getEventoId() != null) {
            Evento evento = eventoDao.findById(certificadoDto.getEventoId()).orElse(null);
            certificado.setEvento(evento);
        }
        if (certificadoDto.getTipoCertificadoId() != null) {
            TipoCertificado tipoCertificado = tipoCertificadoDao.findById(certificadoDto.getTipoCertificadoId()).orElse(null);
            certificado.setTipoCertificado(tipoCertificado);
        }
        Certificado updatedCertificado = certificadoDao.save(certificado);
        CertificadoDto dto = modelMapper.map(updatedCertificado, CertificadoDto.class);
        if (updatedCertificado.getEvento() != null) {
            dto.setEventoId(updatedCertificado.getEvento().getId());
        }
        if (updatedCertificado.getTipoCertificado() != null) {
            dto.setTipoCertificadoId(updatedCertificado.getTipoCertificado().getId());
        }
        return dto;
    }

    @Override
    public void delete(Long id) {
        certificadoDao.deleteById(id);
    }

    @Override
    public void estado(Long id) {
        certificadoDao.toggleEstado(id);
    }
}