package com.mcmm.service;

import com.mcmm.model.dto.PersonaDto;

public interface IPersona {

    public Iterable<PersonaDto> findAll();

    public PersonaDto findById(Long id);

    public PersonaDto save(PersonaDto personaDto);

    public void delete(PersonaDto personaDto);

    public PersonaDto update(Long id, PersonaDto personaDto);

    public Iterable<PersonaDto> personaNoMiembro();

    public PersonaDto buscarCi(String ci);
}
