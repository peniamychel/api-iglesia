package com.mcmm.service.impl;

import com.mcmm.model.dao.MiembroDao;
import com.mcmm.model.dao.PersonaDao;
import com.mcmm.model.dto.MiembroDto;
import com.mcmm.model.dto.PersonaDto;
import com.mcmm.model.entity.Miembro;
import com.mcmm.model.entity.Persona;
import com.mcmm.service.IMiembro;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
//import java.util.logging.Logger;

@Service
public class MiembroImpl implements IMiembro {
    private static final Logger logger = LoggerFactory.getLogger(MiembroImpl.class);

    private ModelMapper modelMapper = new ModelMapper();

    @Autowired
    private MiembroDao miembroDao;

    @Autowired
    private PersonaDao personaDao;

    @Override
    public MiembroDto create(MiembroDto miembroDto) {
        Miembro miembro = modelMapper.map(miembroDto, Miembro.class);

        // Convertir el personaId en una entidad de Persona antes de guardar
        if (miembroDto.getPersonaId() != null) {
            Persona persona = personaDao.findById(miembroDto.getPersonaId())
                    .orElseThrow(() -> new RuntimeException("Persona no encontrada"));
            miembro.setPersona(persona);
        }

        Miembro savedMiembro = miembroDao.save(miembro);
        MiembroDto miembroDtoSave = modelMapper.map(savedMiembro, MiembroDto.class);
        miembroDtoSave.setPersonaId(miembro.getPersona().getId());
        return miembroDtoSave;
    }

    @Override
    public Iterable<MiembroDto> findAll() {
        List<MiembroDto> miembroDtos = new ArrayList<>();
        Iterable<Miembro> miembros = miembroDao.findAll();

        for (Miembro miembro : miembros) {
            MiembroDto miembroDto = modelMapper.map(miembro, MiembroDto.class);
            // Mapear la entidad Persona al DTO
            PersonaDto personaDto = modelMapper.map(miembro.getPersona(), PersonaDto.class);
            miembroDto.setPersonaDto(personaDto);
            miembroDtos.add(miembroDto);
        }
        return miembroDtos;
    }

    @Override
    public MiembroDto findById(Long id) {
        Miembro miembro = miembroDao.findById(id).orElse(null);
        if (miembro != null) {
            MiembroDto miembroDto = modelMapper.map(miembro, MiembroDto.class);
            // Mapear la entidad Persona al DTO
            PersonaDto personaDto = modelMapper.map(miembro.getPersona(), PersonaDto.class);
            miembroDto.setPersonaDto(personaDto);
            return miembroDto;
        }
        return null;
    }


    @Override
    public MiembroDto update(MiembroDto miembroDto) {
        Miembro miembroExistente = miembroDao.findById(miembroDto.getId()).orElse(null);

//        logger.debug("\n \n MIEMBRO ENCONTRADO..................................: {} \n \n", miembroExistente);

        // Si el miembro no existe, retorna null o lanza una excepción, según prefieras manejarlo
        if (miembroExistente == null) {
            return null; // O podrías lanzar una excepción específica
        }

        // Actualiza los campos del miembro existente con los valores del miembroDto proporcionado
        miembroExistente.setFechaConvercion(miembroDto.getFechaConvercion());
        miembroExistente.setLugarConvercion(miembroDto.getLugarConvercion());
        miembroExistente.setInterventores(miembroDto.getInterventores());
        miembroExistente.setDetalles(miembroDto.getDetalles());
        miembroExistente.setEstado(miembroDto.getEstado());


        // Guarda el miembro actualizado en la base de datos
        Miembro miembroActualizado = miembroDao.save(miembroExistente);

        // Retorna el DTO del miembro actualizado
        return modelMapper.map(miembroActualizado, MiembroDto.class);
    }

    @Override
    public void delete(Long id) {
        Miembro miembro = miembroDao.findById(id).orElse(null);
        if (miembro != null) {
            miembroDao.delete(miembro);
        } else {
            throw new EntityNotFoundException("Miembro con ID " + id + " no encontrado.");
        }
    }

    @Override
    public MiembroDto estado(Long id) {
        Miembro miembro = miembroDao.findById(id).orElse(null);
        if (miembro != null) {
            miembro.setEstado(!miembro.getEstado());
            miembroDao.save(miembro);
        } else {
            throw new EntityNotFoundException("Miembro con ID " + id + " no encontrado.");
        }
        return modelMapper.map(miembro, MiembroDto.class);
    }
}
