package com.mcmm.service.impl;

import com.mcmm.model.dao.PrivilegioDao;
import com.mcmm.model.dto.PrivilegioDto;
import com.mcmm.model.entity.Privilegio;
import com.mcmm.service.IPrivilegio;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PrivilegioImpl implements IPrivilegio {

    @Autowired
    private PrivilegioDao privilegioDao;
    private ModelMapper modelMapper = new ModelMapper(); // Inyección de ModelMapper

    @Override
    public Iterable<PrivilegioDto> findAll() {
        List<PrivilegioDto> privilegiosDto = new ArrayList<>();
        Iterable<Privilegio> privilegios = privilegioDao.findAll();

        for (Privilegio privilegio : privilegios) {
            PrivilegioDto dto = modelMapper.map(privilegio, PrivilegioDto.class);
            privilegiosDto.add(dto);
        }
        return privilegiosDto;
    }

    @Override
    public PrivilegioDto findById(Long id) {
        Privilegio privilegio = privilegioDao.findById(id).orElse(null);
        if (privilegio != null) {
            return modelMapper.map(privilegio, PrivilegioDto.class);
        }
        return null; // Considera lanzar una excepción si no se encuentra
    }

    @Override
    public PrivilegioDto save(PrivilegioDto privilegioDto) {
        Privilegio privilegio = modelMapper.map(privilegioDto, Privilegio.class);
        Privilegio savedPrivilegio = privilegioDao.save(privilegio);
        return modelMapper.map(savedPrivilegio, PrivilegioDto.class);
    }

    @Override
    public void delete(PrivilegioDto privilegioDto) {
        Privilegio privilegio = modelMapper.map(privilegioDto, Privilegio.class);
        privilegioDao.delete(privilegio);
    }

    @Override
    public PrivilegioDto update(Long id, PrivilegioDto privilegioDto) {
        Privilegio privilegioExistente = privilegioDao.findById(id)
                .orElseThrow(() -> new RuntimeException("Privilegio no encontrado con ID: " + id));

        // Actualizar los campos
        privilegioExistente.setNombre(privilegioDto.getNombre());
        privilegioExistente.setActo(privilegioDto.getActo());
        privilegioExistente.setEstado(privilegioDto.getEstado());

        Privilegio privilegioActualizado = privilegioDao.save(privilegioExistente);
        return modelMapper.map(privilegioActualizado, PrivilegioDto.class);
    }
}
