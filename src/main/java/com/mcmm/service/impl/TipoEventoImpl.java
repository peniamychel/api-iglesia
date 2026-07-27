package com.mcmm.service.impl;

import com.mcmm.model.dto.tipoEvento.TipoEventoDto;
import com.mcmm.model.entity.TipoEvento;
import com.mcmm.model.dao.TipoEventoDao;
import com.mcmm.service.ITipoEvento;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class TipoEventoImpl implements ITipoEvento {

    private final TipoEventoDao tipoEventoDao;
    private final ModelMapper modelMapper = new ModelMapper();

    public TipoEventoImpl(TipoEventoDao tipoEventoDao) {
        this.tipoEventoDao = tipoEventoDao;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TipoEventoDto> findAll() {
        return tipoEventoDao.findAll().stream()
                .map(tipoEvento -> modelMapper.map(tipoEvento, TipoEventoDto.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TipoEventoDto findById(Long id) {
        TipoEvento tipoEvento = tipoEventoDao.findById(id).orElse(null);
        if (tipoEvento == null) return null;
        return modelMapper.map(tipoEvento, TipoEventoDto.class);
    }

    @Override
    public TipoEventoDto create(TipoEventoDto tipoEventoDto) {
        TipoEvento tipoEvento = modelMapper.map(tipoEventoDto, TipoEvento.class);
        TipoEvento savedTipoEvento = tipoEventoDao.save(tipoEvento);
        return modelMapper.map(savedTipoEvento, TipoEventoDto.class);
    }

    @Override
    public TipoEventoDto update(TipoEventoDto tipoEventoDto) {
        TipoEvento tipoEvento = modelMapper.map(tipoEventoDto, TipoEvento.class);
        TipoEvento updatedTipoEvento = tipoEventoDao.save(tipoEvento);
        return modelMapper.map(updatedTipoEvento, TipoEventoDto.class);
    }

    @Override
    public void delete(Long id) {
        tipoEventoDao.deleteById(id);
    }

    @Override
    public void estado(Long id) {
        tipoEventoDao.toggleEstado(id);
    }
}