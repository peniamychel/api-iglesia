package com.mcmm.service.impl;

import com.mcmm.exception.NotFoundExceptionResource;
import com.mcmm.model.dao.PersonaDao;
import com.mcmm.model.dto.personaDto.PersonaDto;
import com.mcmm.model.entity.Persona;
import com.mcmm.service.FileStorageService;
import com.mcmm.service.IPersona;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class PersonaImpl implements IPersona {

    private static final String PERSONAS_DIR = "personas/";

    private final ModelMapper modelMapper;
    private final PersonaDao personaDao;
    private final FileStorageService fileStorageService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    @Transactional(readOnly = true)
    public List<PersonaDto> findAll() {
        return StreamSupport.stream(personaDao.findAll().spliterator(), false)
                .map(this::buildDtoWithPhotoUrl)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PersonaDto findById(Long id) {
        Persona persona = personaDao.findById(id)
                .orElseThrow(() -> new NotFoundExceptionResource("Persona", "id", id));
        return buildDtoWithPhotoUrl(persona);
    }

    @Override
    @Transactional
    public PersonaDto save(PersonaDto personaDto) {
        Persona persona = modelMapper.map(personaDto, Persona.class);
        Persona savedPersona = personaDao.save(persona);
        return modelMapper.map(savedPersona, PersonaDto.class);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Persona persona = personaDao.findById(id)
                .orElseThrow(() -> new NotFoundExceptionResource("Persona", "id", id));
        personaDao.delete(persona);
    }

    @Override
    @Transactional
    public PersonaDto update(Long id, PersonaDto personaDto) {
        Persona existing = personaDao.findById(id)
                .orElseThrow(() -> new NotFoundExceptionResource("Persona", "id", id));

        existing.setNombre(personaDto.getNombre());
        existing.setApellido(personaDto.getApellido());
        existing.setCi(personaDto.getCi());
        existing.setFechaNac(personaDto.getFechaNac());
        existing.setCelular(personaDto.getCelular());
        existing.setSexo(personaDto.getSexo());
        existing.setDireccion(personaDto.getDireccion());
        existing.setEstado(personaDto.getEstado());

        Persona saved = personaDao.save(existing);
        return buildDtoWithPhotoUrl(saved);
    }

    @Override
    @Transactional
    public PersonaDto partialUpdate(Long id, PersonaDto partialDto) {
        Persona existing = personaDao.findById(id)
                .orElseThrow(() -> new NotFoundExceptionResource("Persona", "id", id));

        if (partialDto.getNombre() != null) existing.setNombre(partialDto.getNombre());
        if (partialDto.getApellido() != null) existing.setApellido(partialDto.getApellido());
        if (partialDto.getCi() != null) existing.setCi(partialDto.getCi());
        if (partialDto.getFechaNac() != null) existing.setFechaNac(partialDto.getFechaNac());
        if (partialDto.getCelular() != null) existing.setCelular(partialDto.getCelular());
        if (partialDto.getSexo() != null) existing.setSexo(partialDto.getSexo());
        if (partialDto.getDireccion() != null) existing.setDireccion(partialDto.getDireccion());
        if (partialDto.getEstado() != null) existing.setEstado(partialDto.getEstado());

        Persona saved = personaDao.save(existing);
        return modelMapper.map(saved, PersonaDto.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PersonaDto> personaNoMiembro() {
        return personaDao.findPersonasWithoutMiembro().stream()
                .map(persona -> modelMapper.map(persona, PersonaDto.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PersonaDto buscarCi(String ci) {
        Persona persona = personaDao.findByCi(ci);
        if (persona != null) {
            return modelMapper.map(persona, PersonaDto.class);
        }
        return null;
    }

    @Override
    @Transactional
    public String updateProfilePhoto(Long id, MultipartFile file) throws IOException {
        Persona persona = personaDao.findById(id)
                .orElseThrow(() -> new NotFoundExceptionResource("Persona", "id", id));

        if (persona.getUriFoto() != null && !persona.getUriFoto().isBlank()) {
            String uriFoto = persona.getUriFoto();
            if (!uriFoto.endsWith("/")) {
                String fileNameOnly = uriFoto.substring(uriFoto.lastIndexOf("/") + 1);
                if (!fileNameOnly.isBlank()) {
                    fileStorageService.deleteFile(PERSONAS_DIR + fileNameOnly);
                }
            }
        }

        String fileName = fileStorageService.storeFile(file, persona.getNombre(), PERSONAS_DIR);

        String fileUrl = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path(uploadDir)
                .path("/")
                .path(PERSONAS_DIR)
                .path(fileName)
                .toUriString();
        persona.setUriFoto(fileName);
        personaDao.save(persona);
        return fileUrl;
    }

    @Override
    @Transactional
    public void deleteProfilePhoto(Long id) {
        Persona persona = personaDao.findById(id)
                .orElseThrow(() -> new NotFoundExceptionResource("Persona", "id", id));

        if (persona.getUriFoto() != null && !persona.getUriFoto().isBlank()) {
            String uriFoto = persona.getUriFoto();
            if (!uriFoto.endsWith("/")) {
                String fileNameOnly = uriFoto.substring(uriFoto.lastIndexOf("/") + 1);
                if (!fileNameOnly.isBlank()) {
                    try {
                        fileStorageService.deleteFile(PERSONAS_DIR + fileNameOnly);
                    } catch (IOException e) {
                        throw new RuntimeException("Error al eliminar la foto: " + e.getMessage());
                    }
                }
            }
        }

        persona.setUriFoto(null);
        personaDao.save(persona);
    }

    private PersonaDto buildDtoWithPhotoUrl(Persona persona) {
        PersonaDto dto = modelMapper.map(persona, PersonaDto.class);
        if (dto.getUriFoto() != null) {
            String fileUrl = ServletUriComponentsBuilder
                    .fromCurrentContextPath()
                    .path(uploadDir)
                    .path("/")
                    .path(PERSONAS_DIR)
                    .path(dto.getUriFoto())
                    .toUriString();
            dto.setUriFoto(fileUrl);
        }
        return dto;
    }
}
