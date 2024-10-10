package com.mcmm.service.impl;

import com.mcmm.model.dao.MiembroDao;
import com.mcmm.model.dto.MiembroDto;
import com.mcmm.model.entity.Miembro;
import com.mcmm.service.IMiembro;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MiembroImpl implements IMiembro {


    private ModelMapper modelMapper = new ModelMapper();

    @Autowired
    private MiembroDao miembroDao;
    @Override
    public Iterable<MiembroDto> findAll() {
        List<MiembroDto> miembroDto = new ArrayList<>();
        Iterable<Miembro> miembros = miembroDao.findAll();

        for (Miembro miembro : miembros) {
            MiembroDto dto = modelMapper.map(miembro, MiembroDto.class);
            miembroDto.add(dto);
        }
        return miembroDto;
    }

    @Override
    public MiembroDto findById(Long id) {
        Miembro miembro = miembroDao.findById(id).orElse(null);
        if (miembro != null) {
            return modelMapper.map(miembro, MiembroDto.class);
        }
        return null;
    }

    @Override
    public MiembroDto save(MiembroDto miembroDto) {
        Miembro miembro = modelMapper.map(miembroDto, Miembro.class);
        Miembro savedMiembro = miembroDao.save(miembro);
        return modelMapper.map(savedMiembro, MiembroDto.class);
    }

    @Override
    public void delete(MiembroDto miembroDto) {
        Miembro miembro = modelMapper.map(miembroDto, Miembro.class);
        miembroDao.delete(miembro);
    }

    @Override
    public MiembroDto update(Long id, MiembroDto miembroDto) {
        return null;
    }
}
