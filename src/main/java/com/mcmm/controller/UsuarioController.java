package com.mcmm.controller;

import com.mcmm.model.dto.usuarioDto.UsuarioChangePasswordDto;
import com.mcmm.model.dto.usuarioDto.UsuarioResetPasswordDto;
import com.mcmm.model.dto.usuarioDto.UsuarioUpdateDto;
import com.mcmm.model.dto.usuarioDto.UsuarioDto;
import com.mcmm.model.dto.usuarioDto.UsuarioDtoRes;
import com.mcmm.model.payload.ApiResponse;
import com.mcmm.service.IUsuario;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/usuario/v1")
@PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_IGLESIA', 'ENCARGADO_EVENTO')")
@RequiredArgsConstructor
public class UsuarioController {

    private final IUsuario usuarioService;

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_IGLESIA', 'ENCARGADO_EVENTO')")
    public ResponseEntity<ApiResponse<UsuarioDtoRes>> createUsuario(@Valid @RequestBody UsuarioDto usuarioDto) {
        UsuarioDtoRes usuarioCreado = usuarioService.create(usuarioDto);
        return new ResponseEntity<>(
                ApiResponse.<UsuarioDtoRes>builder()
                        .message("Nuevo usuario guardado con éxito")
                        .datos(usuarioCreado)
                        .nombreModelo("Usuario")
                        .build(),
                HttpStatus.CREATED);
    }

    @GetMapping("/findall")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ApiResponse<List<UsuarioDtoRes>>> findAll() {
        List<UsuarioDtoRes> usuarioDtosRes = usuarioService.findAll();
        return ResponseEntity.ok(
                ApiResponse.<List<UsuarioDtoRes>>builder()
                        .message("Listado de Usuarios")
                        .datos(usuarioDtosRes)
                        .nombreModelo("Usuario")
                        .build());
    }

    @DeleteMapping("/delete/{id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_IGLESIA', 'ENCARGADO_EVENTO') AND hasAuthority('Gestionar Usuarios')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        usuarioService.delete(id);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .message("Usuario eliminado exitosamente.")
                        .datos(null)
                        .nombreModelo("Usuario")
                        .build());
    }

    @GetMapping("/showbyid/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<ApiResponse<UsuarioDtoRes>> showById(@PathVariable("id") Long id) {
        UsuarioDtoRes usuarioFiedById = usuarioService.findById(id);
        return ResponseEntity.ok(
                ApiResponse.<UsuarioDtoRes>builder()
                        .message("Usuario encontrado.")
                        .datos(usuarioFiedById)
                        .nombreModelo("Usuario")
                        .build());
    }

    @PutMapping("/update")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_IGLESIA', 'ENCARGADO_EVENTO') AND hasAuthority('Gestionar Usuarios')")
    public ResponseEntity<ApiResponse<UsuarioDtoRes>> updateUser(@Valid @RequestBody UsuarioUpdateDto usuarioUpdateDto) {
        UsuarioDtoRes usuarioActualizado = usuarioService.updateUser(usuarioUpdateDto);
        return ResponseEntity.ok(
                ApiResponse.<UsuarioDtoRes>builder()
                        .message("Usuario actualizado exitosamente.")
                        .datos(usuarioActualizado)
                        .nombreModelo("Usuario")
                        .build());
    }

    @PutMapping("/update-roles")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_IGLESIA', 'ENCARGADO_EVENTO') AND hasAuthority('Gestionar Usuarios')")
    public ResponseEntity<ApiResponse<UsuarioDtoRes>> updateUserRoles(@RequestBody UsuarioDto usuarioDto) {
        UsuarioDtoRes usuarioActualizado = usuarioService.updateUserRoles(usuarioDto);
        return ResponseEntity.ok(
                ApiResponse.<UsuarioDtoRes>builder()
                        .message("Roles de usuario actualizados exitosamente.")
                        .datos(usuarioActualizado)
                        .nombreModelo("Usuario")
                        .build());
    }

    @PutMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> changePassword(@RequestBody @Valid UsuarioChangePasswordDto usuarioChangePasswordDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        usuarioService.changePassword(usuarioChangePasswordDto, currentUsername);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .message("Contraseña cambiada exitosamente.")
                        .datos(null)
                        .nombreModelo("Usuario")
                        .build());
    }

    @PutMapping("/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@RequestBody @Valid UsuarioResetPasswordDto usuarioResetPasswordDto) {
        usuarioService.resetPassword(usuarioResetPasswordDto);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .message("Contraseña restablecida exitosamente.")
                        .datos(null)
                        .nombreModelo("Usuario")
                        .build());
    }

    @PostMapping(value = "/{id}/foto", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_IGLESIA', 'ENCARGADO_EVENTO')")
    public ResponseEntity<ApiResponse<String>> uploadProfilePhoto(
            @PathVariable Long id,
            @RequestPart("file") MultipartFile file) {
        try {
            String fileUrl = usuarioService.updateProfilePhoto(id, file);
            return ResponseEntity.ok(
                    ApiResponse.<String>builder()
                            .message("Foto de perfil actualizada exitosamente.")
                            .datos(fileUrl)
                            .nombreModelo("Usuario")
                            .build());
        } catch (IOException e) {
            throw new RuntimeException("Error al subir la foto: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}/foto")
    @PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_IGLESIA', 'ENCARGADO_EVENTO')")
    public ResponseEntity<ApiResponse<Void>> deleteProfilePhoto(@PathVariable Long id) {
        usuarioService.deleteProfilePhoto(id);
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .message("Foto de perfil eliminada exitosamente.")
                        .datos(null)
                        .nombreModelo("Usuario")
                        .build());
    }

    @GetMapping("/findbyusername")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UsuarioDtoRes>> findByUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        UsuarioDtoRes usuarioFiedById = usuarioService.findByUsername(currentUsername);
        return ResponseEntity.ok(
                ApiResponse.<UsuarioDtoRes>builder()
                        .message("Usuario encontrado.")
                        .datos(usuarioFiedById)
                        .nombreModelo("Usuario")
                        .build());
    }
}
