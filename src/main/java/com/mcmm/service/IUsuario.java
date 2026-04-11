package com.mcmm.service;

import com.mcmm.model.dto.usuarioDto.UsuarioChangePasswordDto;
import com.mcmm.model.dto.usuarioDto.UsuarioUpdateDto;
import com.mcmm.model.dto.usuarioDto.UsuarioDto;
import com.mcmm.model.dto.usuarioDto.UsuarioDtoRes;
import com.mcmm.model.entity.Usuario;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface IUsuario {
    final String USUARIOS_DIR = "usuarios/";

    public Iterable<UsuarioDtoRes> findAll();

    public UsuarioDtoRes findById(Long id);

    public UsuarioDto create(Usuario usuario);

    public void delete(Long id);

    Usuario updateUserRoles(UsuarioDto usuarioDto);


    Usuario updateUser(UsuarioUpdateDto usuarioUpdateDto);

    void changePassword(UsuarioChangePasswordDto usuarioChangePasswordDto, String currentUsername);

    String updateProfilePhoto(Long userId, MultipartFile file) throws IOException;

}
