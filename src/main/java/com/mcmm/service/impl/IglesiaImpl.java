package com.mcmm.service.impl;

import com.mcmm.model.dao.IglesiaDao;
import com.mcmm.model.dto.IglesiaDto;
import com.mcmm.model.dto.PersonaDto;
import com.mcmm.model.entity.Iglesia;
import com.mcmm.model.entity.Persona;
import com.mcmm.service.IIglesia;
import org.modelmapper.ModelMapper;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class IglesiaImpl implements IIglesia {

    @Autowired
    private IglesiaDao iglesiaDao;
    private ModelMapper modelMapper = new ModelMapper();
    @Override
    public List<IglesiaDto> findAll() {
        List<IglesiaDto> iglesiasDto = new ArrayList<>();
        Iterable<Iglesia> iglesias = iglesiaDao.findAll();

        for (Iglesia iglesia : iglesias){
            IglesiaDto dto = modelMapper.map(iglesia, IglesiaDto.class);
            iglesiasDto.add(dto);
        }
        return iglesiasDto;
    }
    @Override
    public IglesiaDto save(IglesiaDto iglesiaDto) {
        Iglesia iglesia = modelMapper.map(iglesiaDto, Iglesia.class);
        Iglesia savedIglesia = iglesiaDao.save(iglesia);
        return modelMapper.map(savedIglesia, IglesiaDto.class);
    }

    @Override
    public IglesiaDto findById(Long id) {
        Iglesia iglesia = iglesiaDao.findById(id).orElse(null);
        if (iglesia != null) {
            return modelMapper.map(iglesia, IglesiaDto.class);
        }
        return null;
    }



    @Override
    public void delete(IglesiaDto iglesiaDto) {
        Iglesia iglesia = modelMapper.map(iglesiaDto, Iglesia.class);
        iglesiaDao.delete(iglesia);
    }

    @Override
    public IglesiaDto update(Long id, IglesiaDto iglesiaDto) {
        return null;
    }

    @Override
    public IglesiaDto buscarNombreIglesia(String nameIglesia) {
        Iglesia iglesia = iglesiaDao.buscarPorNombreIglesia(nameIglesia);
        if (iglesia != null) {
            return modelMapper.map(iglesia, IglesiaDto.class);
        }
        return null;
    }

}
