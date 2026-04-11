package com.mcmm.service.impl;
import com.mcmm.model.dao.PersonaDao;
import com.mcmm.model.dto.personaDto.PersonaDto;
import com.mcmm.model.entity.Persona;
import com.mcmm.service.FileStorageService;
import com.mcmm.service.IPersona;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
@Service
public class PersonaImpl implements IPersona {

    @Autowired
    private PersonaDao personaDao;

    @Autowired
    private FileStorageService fileStorageService;

    @Value("${file.upload-dir}")
    private String uploadDir;
    private ModelMapper modelMapper = new ModelMapper();


    @Override
    public Iterable<PersonaDto> findAll() {
        List<PersonaDto> personasDto = new ArrayList<>();
        Iterable<Persona> personas = personaDao.findAll();

        for (Persona persona : personas) {
            PersonaDto dto = modelMapper.map(persona, PersonaDto.class);
            if (persona.getUriFoto() != null) {
                String fileUrl = ServletUriComponentsBuilder
                        .fromCurrentContextPath()
                        .path(uploadDir)
                        .path("/")
                        .path(PERSONAS_DIR)
                        .path(dto.getUriFoto())
                        .toUriString();
                dto.setUriFoto(fileUrl);
            }
            personasDto.add(dto);
        }
        return personasDto;
    }

    @Override
    public PersonaDto findById(Long id) {
        Persona persona = personaDao.findById(id).orElse(null);
        if (persona != null) {
            PersonaDto dto = modelMapper.map(persona, PersonaDto.class);
            String fileUrl = ServletUriComponentsBuilder
                    .fromCurrentContextPath()
                    .path(uploadDir)
                    .path("/")
                    .path(PERSONAS_DIR)
                    .path(dto.getUriFoto())
                    .toUriString();
            dto.setUriFoto(fileUrl);
            return dto;
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
        // Actualización completa: Busca existente, copia TODOS los campos (sobrescribe nulls), guarda
        Persona existing = personaDao.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Persona no encontrada con id: " + id));

        // Copia todas las propiedades del DTO a la entidad, excluyendo 'id'
        BeanUtils.copyProperties(personaDto, existing, "id");

        Persona saved = personaDao.save(existing);
        return buildDtoWithPhotoUrl(saved);  // Método helper reutilizable (ver abajo)
    }

    @Override
    @Transactional  // NUEVO: Para atomicidad en actualizaciones parciales
    public PersonaDto partialUpdate(Long id, PersonaDto partialDto) {
        // Actualización parcial: Solo copia campos NO nulos del DTO
        Persona existing = personaDao.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Persona no encontrada con id: " + id));

        // Copia manual solo si el campo en DTO NO es null (evita sobrescribir con null)
        if (partialDto.getNombre() != null) {
            existing.setNombre(partialDto.getNombre());
        }
        if (partialDto.getApellido() != null) {
            existing.setApellido(partialDto.getApellido());
        }
        if (partialDto.getCi() != null) {
            existing.setCi(partialDto.getCi());
        }
        if (partialDto.getFechaNac() != null) {
            existing.setFechaNac(partialDto.getFechaNac());
        }
        if (partialDto.getCelular() != null) {
            existing.setCelular(partialDto.getCelular());
        }
        if (partialDto.getSexo() != null) {
            existing.setSexo(partialDto.getSexo());
        }
        if (partialDto.getDireccion() != null) {
            existing.setDireccion(partialDto.getDireccion());
        }
        // uriFoto NO se actualiza aquí (usa endpoint separado)
        if (partialDto.getEstado() != null) {
            existing.setEstado(partialDto.getEstado());
        }
        // createdAt no se toca (updatable=false en entidad)

        Persona saved = personaDao.save(existing);
        return modelMapper.map(saved, PersonaDto.class);
    }

    @Override
    public Iterable<PersonaDto> personaNoMiembro() {
        List<PersonaDto> personasDto = new ArrayList<>();
        Iterable<Persona> personas = personaDao.findPersonasWithoutMiembro();

        for (Persona persona : personas) {
            PersonaDto dto = modelMapper.map(persona, PersonaDto.class);
            personasDto.add(dto);
        }
        return personasDto;
    }

    @Override
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
                .orElseThrow(() -> new EntityNotFoundException("Persona no encontrada: " + id));


        // Si existe una foto anterior, la eliminamos
        if (persona.getUriFoto() != null) {
            String oldFileName = PERSONAS_DIR + persona.getUriFoto().substring(persona.getUriFoto().lastIndexOf("/") + 1);
            fileStorageService.deleteFile(oldFileName);
        }
        // Guardamos la nueva foto
        String fileName = fileStorageService.storeFile(file, persona.getNombre(), PERSONAS_DIR);

        // Construimos la URL completa
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

    // MÉTODO HELPER NUEVO: Reutiliza la lógica de construcción de URL de foto (evita duplicación)
    private PersonaDto buildDtoWithPhotoUrl(Persona persona) {
        PersonaDto dto = modelMapper.map(persona, PersonaDto.class);
        if (null != null) {
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
