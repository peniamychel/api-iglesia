package com.mcmm.service.impl;

import com.mcmm.model.dao.UsuarioDao;
import com.mcmm.model.dto.PersonaDto;
import com.mcmm.model.dto.UsuarioDto;
import com.mcmm.model.entity.Persona;
import com.mcmm.model.entity.Usuario;
import com.mcmm.service.IUsuario;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UsuarioImpl implements IUsuario {

    @Autowired
    private UsuarioDao usuarioDao;
    private ModelMapper modelMapper = new ModelMapper();
    @Override
    public Iterable<UsuarioDto> findAll() {
        List<UsuarioDto> usuarioDto = new ArrayList<>();
        Iterable<Usuario> usuarios = usuarioDao.findAll();

        for (Usuario usuario : usuarios) {
            UsuarioDto dto = modelMapper.map(usuario, UsuarioDto.class);
            usuarioDto.add(dto);
        }
        return usuarioDto;
    }

    @Override
    public UsuarioDto findById(Long id) {
        Usuario usuario = usuarioDao.findById(id).orElse(null);
        if (usuario != null) {
            return modelMapper.map(usuario, UsuarioDto.class);
        }
        return null;
    }

    @Override
    public UsuarioDto create(UsuarioDto usuarioDto) {
        return null;
    }

    @Override
    public void delete(Long id) {

    }

    @Override
    public UsuarioDto update(Long id, UsuarioDto usuarioDto) {
        return null;
    }
}
