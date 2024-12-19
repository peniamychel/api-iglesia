package com.mcmm.service;

import com.mcmm.model.dto.usuarioDto.ChangePasswordDto;
import com.mcmm.model.dto.usuarioDto.UpdateUserDto;
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


    Usuario updateUser(UpdateUserDto updateUserDto);

    void changePassword(ChangePasswordDto changePasswordDto, String currentUsername);

    String updateProfilePhoto(Long userId, MultipartFile file) throws IOException;

}
