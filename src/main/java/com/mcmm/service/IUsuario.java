package com.mcmm.service;

import com.mcmm.model.dto.usuarioDto.UsuarioChangePasswordDto;
import com.mcmm.model.dto.usuarioDto.UsuarioResetPasswordDto;
import com.mcmm.model.dto.usuarioDto.UsuarioUpdateDto;
import com.mcmm.model.dto.usuarioDto.UsuarioDto;
import com.mcmm.model.dto.usuarioDto.UsuarioDtoRes;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface IUsuario {

    List<UsuarioDtoRes> findAll();

    UsuarioDtoRes findById(Long id);

    UsuarioDtoRes findByUsername(String username);

    UsuarioDtoRes create(UsuarioDto usuarioDto);

    void delete(Long id);

    UsuarioDtoRes updateUserRoles(UsuarioDto usuarioDto);

    UsuarioDtoRes updateUser(UsuarioUpdateDto usuarioUpdateDto);

    void changePassword(UsuarioChangePasswordDto usuarioChangePasswordDto, String currentUsername);

    void resetPassword(UsuarioResetPasswordDto usuarioResetPasswordDto);

    String updateProfilePhoto(Long userId, MultipartFile file) throws IOException;

    void deleteProfilePhoto(Long id);
}
