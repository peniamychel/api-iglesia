package com.mcmm.service.impl;

import com.mcmm.exception.NotFoundExceptionResource;
import com.mcmm.model.dao.MiembroDao;
import com.mcmm.model.dao.PersonaDao;
import com.mcmm.model.dto.MiembroDto.MiembroDto;
import com.mcmm.model.dto.personaDto.PersonaDto;
import com.mcmm.model.entity.Miembro;
import com.mcmm.model.entity.Persona;
import com.mcmm.service.IMiembro;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MiembroImpl implements IMiembro {

    private final ModelMapper modelMapper;
    private final MiembroDao miembroDao;
    private final PersonaDao personaDao;

    @Override
    @Transactional
    public MiembroDto create(MiembroDto miembroDto) {
        Miembro miembro = modelMapper.map(miembroDto, Miembro.class);

        // Convertir el personaId en una entidad de Persona antes de guardar
        if (miembroDto.getPersonaId() != null) {
            Persona persona = personaDao.findById(miembroDto.getPersonaId())
                    .orElseThrow(() -> new NotFoundExceptionResource("Persona", "id", miembroDto.getPersonaId()));
            miembro.setPersona(persona);
        }

        Miembro savedMiembro = miembroDao.save(miembro);
        MiembroDto miembroDtoSave = modelMapper.map(savedMiembro, MiembroDto.class);
        miembroDtoSave.setPersonaId(miembro.getPersona().getId());
        return miembroDtoSave;
    }

    @Override
    @Transactional(readOnly = true)
    public Iterable<MiembroDto> findAll() {
        List<MiembroDto> miembroDtos = new ArrayList<>();
        Iterable<Miembro> miembros = miembroDao.findAll();

        for (Miembro miembro : miembros) {
            MiembroDto miembroDto = modelMapper.map(miembro, MiembroDto.class);
            if (miembro.getPersona() != null) {
                PersonaDto personaDto = modelMapper.map(miembro.getPersona(), PersonaDto.class);
                miembroDto.setPersonaDto(personaDto);
            }
            miembroDtos.add(miembroDto);
        }
        return miembroDtos;
    }

    @Override
    @Transactional(readOnly = true)
    public MiembroDto findById(Long id) {
        Miembro miembro = miembroDao.findById(id)
                .orElseThrow(() -> new NotFoundExceptionResource("Miembro", "id", id));
        
        MiembroDto miembroDto = modelMapper.map(miembro, MiembroDto.class);
        if (miembro.getPersona() != null) {
            PersonaDto personaDto = modelMapper.map(miembro.getPersona(), PersonaDto.class);
            miembroDto.setPersonaDto(personaDto);
        }
        return miembroDto;
    }

    @Override
    @Transactional
    public MiembroDto update(MiembroDto miembroDto) {
        Miembro miembroExistente = miembroDao.findById(miembroDto.getId())
                .orElseThrow(() -> new NotFoundExceptionResource("Miembro", "id", miembroDto.getId()));

        miembroExistente.setFechaConvercion(miembroDto.getFechaConvercion());
        miembroExistente.setLugarConvercion(miembroDto.getLugarConvercion());
        miembroExistente.setInterventores(miembroDto.getInterventores());
        miembroExistente.setDetalles(miembroDto.getDetalles());
        miembroExistente.setEstado(miembroDto.getEstado());

        if (miembroDto.getPersonaId() != null) {
            Persona persona = personaDao.findById(miembroDto.getPersonaId())
                    .orElseThrow(() -> new NotFoundExceptionResource("Persona", "id", miembroDto.getPersonaId()));
            miembroExistente.setPersona(persona);
        }

        Miembro miembroActualizado = miembroDao.save(miembroExistente);
        return modelMapper.map(miembroActualizado, MiembroDto.class);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Miembro miembro = miembroDao.findById(id)
                .orElseThrow(() -> new NotFoundExceptionResource("Miembro", "id", id));
        miembroDao.delete(miembro);
    }

    @Override
    @Transactional
    public MiembroDto estado(Long id) {
        Miembro miembro = miembroDao.findById(id)
                .orElseThrow(() -> new NotFoundExceptionResource("Miembro", "id", id));
        miembro.setEstado(!miembro.getEstado());
        Miembro updated = miembroDao.save(miembro);
        return modelMapper.map(updated, MiembroDto.class);
    }
}
