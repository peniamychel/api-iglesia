package com.mcmm.service;

import com.mcmm.model.dto.personaDto.PersonaDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface IPersona {

    List<PersonaDto> findAll();

    PersonaDto findById(Long id);

    PersonaDto save(PersonaDto personaDto);

    void delete(Long id);

    PersonaDto update(Long id, PersonaDto personaDto);

    PersonaDto partialUpdate(Long id, PersonaDto partialDto);

    List<PersonaDto> personaNoMiembro();

    PersonaDto buscarCi(String ci);

    String updateProfilePhoto(Long id, MultipartFile file) throws IOException;

    void deleteProfilePhoto(Long id);
}
