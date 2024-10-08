package com.mcmm.service.impl;

import com.mcmm.model.dao.PersonaDao;
import com.mcmm.model.dto.IglesiaDto;
import com.mcmm.model.dto.PersonaDto;
import com.mcmm.model.entity.Iglesia;
import com.mcmm.model.entity.Persona;
import com.mcmm.service.IPersona;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PersonaImpl implements IPersona {

    @Autowired
    private PersonaDao personaDao;
    private ModelMapper modelMapper = new ModelMapper();


    @Override
    public Iterable<PersonaDto> findAll() {
        List<PersonaDto> personasDto = new ArrayList<>();
        Iterable<Persona> personas = personaDao.findAll();

        for (Persona persona : personas) {
            PersonaDto dto = modelMapper.map(persona, PersonaDto.class);
            personasDto.add(dto);
        }
        return personasDto;
    }

    @Override
    public PersonaDto findById(Long id) {
        Persona persona = personaDao.findById(id).orElse(null);
        if (persona != null) {
            return modelMapper.map(persona, PersonaDto.class);
        }
        return null;
    }

    @Override
    public PersonaDto save(PersonaDto personaDto) {
        Persona persona = modelMapper.map(personaDto, Persona.class);
        Persona savedPersona = personaDao.save(persona);
        return modelMapper.map(savedPersona, PersonaDto.class);
    }

    @Override
    public void delete(PersonaDto personaDto) {
        Persona persona = modelMapper.map(personaDto, Persona.class);
        personaDao.delete(persona);
    }

    @Override
    public PersonaDto update(Long id, PersonaDto personaDto) {
        return null;
    }
}
