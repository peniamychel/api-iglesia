package com.mcmm.service;

import com.mcmm.model.dto.personaDto.PersonaDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface IPersona {

    final String PERSONAS_DIR = "personas/";

    public Iterable<PersonaDto> findAll();

    public PersonaDto findById(Long id);

    public PersonaDto save(PersonaDto personaDto);

    public void delete(PersonaDto personaDto);

    public PersonaDto update(Long id, PersonaDto personaDto);

    public PersonaDto partialUpdate(Long id, PersonaDto partialDto);

    public Iterable<PersonaDto> personaNoMiembro();

    public PersonaDto buscarCi(String ci);

    String updateProfilePhoto(Long id, MultipartFile file) throws IOException;
}
