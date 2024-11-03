package com.mcmm.service;

import com.mcmm.model.dto.MiembroDto;
import com.mcmm.model.dto.UsuarioDto;

public interface IUsuario {

    public Iterable<UsuarioDto> findAll();

    public UsuarioDto findById(Long id);

    public UsuarioDto create(UsuarioDto usuarioDto);

    public void delete(Long id);

    public UsuarioDto update(Long id, UsuarioDto usuarioDto);
}
