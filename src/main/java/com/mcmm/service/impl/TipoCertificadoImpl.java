package com.mcmm.service.impl;

import com.mcmm.model.dto.tipoCertificado.TipoCertificadoDto;
import com.mcmm.model.entity.TipoCertificado;
import com.mcmm.model.dao.TipoCertificadoDao;
import com.mcmm.service.ITipoCertificado;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TipoCertificadoImpl implements ITipoCertificado {

    private final TipoCertificadoDao tipoCertificadoDao;
    private final ModelMapper modelMapper = new ModelMapper();

    public TipoCertificadoImpl(TipoCertificadoDao tipoCertificadoDao) {
        this.tipoCertificadoDao = tipoCertificadoDao;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TipoCertificadoDto> findAll() {
        return tipoCertificadoDao.findAll().stream()
                .map(tipoCertificado -> modelMapper.map(tipoCertificado, TipoCertificadoDto.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TipoCertificadoDto findById(Long id) {
        TipoCertificado tipoCertificado = tipoCertificadoDao.findById(id).orElse(null);
        if (tipoCertificado == null) return null;
        return modelMapper.map(tipoCertificado, TipoCertificadoDto.class);
    }

    @Override
    public TipoCertificadoDto create(TipoCertificadoDto tipoCertificadoDto) {
        TipoCertificado tipoCertificado = modelMapper.map(tipoCertificadoDto, TipoCertificado.class);
        TipoCertificado savedTipoCertificado = tipoCertificadoDao.save(tipoCertificado);
        return modelMapper.map(savedTipoCertificado, TipoCertificadoDto.class);
    }

    @Override
    public TipoCertificadoDto update(TipoCertificadoDto tipoCertificadoDto) {
        TipoCertificado tipoCertificado = modelMapper.map(tipoCertificadoDto, TipoCertificado.class);
        TipoCertificado updatedTipoCertificado = tipoCertificadoDao.save(tipoCertificado);
        return modelMapper.map(updatedTipoCertificado, TipoCertificadoDto.class);
    }

    @Override
    public void delete(Long id) {
        tipoCertificadoDao.deleteById(id);
    }

    @Override
    public void estado(Long id) {
        tipoCertificadoDao.toggleEstado(id);
    }
}