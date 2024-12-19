package com.mcmm.controller;


import com.mcmm.model.dao.UsuarioDao;
import com.mcmm.model.dto.usuarioDto.ChangePasswordDto;
import com.mcmm.model.dto.usuarioDto.UpdateUserDto;
import com.mcmm.model.dto.usuarioDto.UsuarioDto;
import com.mcmm.model.dto.usuarioDto.UsuarioDtoRes;
import com.mcmm.model.entity.ERole;
import com.mcmm.model.entity.Rol;
import com.mcmm.model.entity.Usuario;
import com.mcmm.model.payload.MessageResponse;
import com.mcmm.model.payload.MessageResponseLogin;
import com.mcmm.service.IRol;
import com.mcmm.service.IUsuario;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/usuario/v1")
@PreAuthorize("hasAnyRole('ADMIN', 'ENCARGADO_IGLESIA', 'ENCARGADO_EVENTO')")
public class UsuarioController {

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UsuarioDao usuarioDao;
    @Autowired
    private IRol rolDao;

    @Autowired
    private IUsuario usuarioService;

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> createUsuario(@Valid @RequestBody UsuarioDto usuarioDto) {

        // Usar un Set para eliminar duplicados
        Set<ERole> rolesSet = usuarioDto.getRoles().stream()
                .map(ERole::valueOf) // Convertir a ERole directamente
                .collect(Collectors.toSet()); // Eliminar duplicados automáticamente

        // Convertir el Set de ERole a Set de Rol
        Set<Rol> roles = rolesSet.stream()
                .map(role -> Rol.builder()
                        .name(role)
                        .build())
                .collect(Collectors.toSet());

        Usuario usuario = Usuario.builder()
                .username(usuarioDto.getUsername())
                .name(usuarioDto.getName())
                .apellidos(usuarioDto.getApellidos())
                .email(usuarioDto.getEmail())
                .password(usuarioDto.getPassword())
                .roles(roles)
                .build();

        try {
            if (usuarioDao.existsByUsername(usuario.getUsername())) {
                return new ResponseEntity<>(
                        MessageResponseLogin.builder()
                                .success(false)
                                .message("El usuario ya existe")
                                .datos(null)
                                .build()
                        , HttpStatus.OK
                );
            }

            if (usuarioDao.existsByEmail(usuario.getEmail())) {
                return new ResponseEntity<>(
                        MessageResponseLogin.builder()
                                .success(false)
                                .message("El email ya existe")
                                .datos(null)
                                .build()
                        , HttpStatus.OK
                );
            }

            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));

            // Manejar roles
            Set<Rol> rolesm;
            if (usuario.getRoles() == null || usuario.getRoles().isEmpty()) {
                // Asignar rol por defecto
                rolesm = Collections.singleton(rolDao.findOrCreateRol(ERole.ENCARGADO_EVENTO));
            } else {
                // Obtener roles existentes
                Set<ERole> roleNames = usuario.getRoles().stream()
                        .map(Rol::getName)
                        .collect(Collectors.toSet());

                // Usar roles existentes
                rolesm = rolDao.getRolesByNames(roleNames);
            }

            usuario.setRoles(rolesm);

            UsuarioDto guardado2 = usuarioService.create(usuario);

//            Usuario guardado = usuarioDao.save(usuario);
            if (guardado2.getId() != null) {
                usuarioDao.save(usuario);
            }
            return new ResponseEntity<>(
                    MessageResponseLogin.builder()
                            .success(true)
                            .message("Nuevo usuario guardado con exito")
                            .datos(guardado2)
                            .build()
                    , HttpStatus.OK
            );

        } catch (Exception e) {
            return new ResponseEntity<>(
                    MessageResponseLogin.builder()
                            .message("Error 403")
                            .datos(null)
                            .build()
                    , HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    @GetMapping("/findall")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> findAll() {
        ResponseEntity<?> responseEntity;
        Iterable<UsuarioDtoRes> usuarioDtosRes = usuarioService.findAll();
        try {
            responseEntity = new ResponseEntity<>(
                    MessageResponse.builder()
                            .message("Listado de Usuarios")
                            .datos(usuarioDtosRes)
                            .nombreModelo("Usuario")
                            .build()
                    , HttpStatus.OK
            );
        } catch (Exception e) {
            responseEntity = new ResponseEntity<>(
                    MessageResponse.builder()
                            .message("No se encontro datos.")
                            .datos(null)
                            .build()
                    , HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
        return responseEntity;
    }

    @DeleteMapping("/delete")
    public String eliminarUsuaior(@RequestParam String id) {
        usuarioDao.deleteById(Long.parseLong(id));
        return "Usuario eliminado".concat(id);
    }

    @GetMapping("/showbyid/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> showById(@PathVariable("id") Long id) {
        ResponseEntity<?> responseEntity;
        try {
            UsuarioDtoRes usuarioFiedById = usuarioService.findById(id);
            responseEntity = new ResponseEntity<>(
                    MessageResponse.builder()
                            .message("Usurio encontrada.")
                            .datos(usuarioFiedById)
                            .nombreModelo("Usuario")
                            .build(),
                    HttpStatus.OK
            );
        } catch (DataAccessException e) {
            responseEntity = new ResponseEntity<>(
                    MessageResponse.builder()
                            .message("Error al buscar la Usuario.")
                            .datos(null)
                            .build(),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
        return responseEntity;
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateUser(@Valid @RequestBody UpdateUserDto updateUserDto) {
        return ResponseEntity.ok(usuarioService.updateUser(updateUserDto));
    }

    @PutMapping("/update-roles")
    public ResponseEntity<?> updateUserRoles(@RequestBody UsuarioDto usuarioDto) {
        return ResponseEntity.ok(usuarioService.updateUserRoles(usuarioDto));
    }

//    @PutMapping("/change-password")
//    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordDto changePasswordDto) {
//        usuarioService.changePassword(changePasswordDto);
//        return ResponseEntity.ok().build();
//    }

    @PutMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> changePassword( @RequestBody ChangePasswordDto changePasswordDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();

        usuarioService.changePassword(changePasswordDto, currentUsername);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Password changed successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/foto")
    public ResponseEntity<?> uploadProfilePhoto(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        try {
            String fileUrl = usuarioService.updateProfilePhoto(id, file);
            Map<String, String> response = new HashMap<>();
            response.put("uriFoto", fileUrl);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/findbyusername")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> findByUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        Usuario usuario = new Usuario();

        Optional<Usuario> optionalUsuario = usuarioDao.findByUsername(currentUsername);
        if (optionalUsuario.isPresent()) {
            usuario = optionalUsuario.get();
            System.out.println("Usuario encontrado: " + usuario.getUsername());
        } else {
            System.out.println("Usuario no encontrado");
        }
//        Map<String, String> response = new HashMap<>();
//        response.put("message", "Password changed successfully");
//        response.put("username", usuario.getId().toString());

        ResponseEntity<?> responseEntity;
        try {
            UsuarioDtoRes usuarioFiedById = usuarioService.findById(usuario.getId());
            usuarioFiedById.setPassword("pass oculto");
            responseEntity = new ResponseEntity<>(
                    MessageResponse.builder()
                            .message("Usurio encontrada.")
                            .datos(usuarioFiedById)
                            .nombreModelo("Usuario")
                            .build(),
                    HttpStatus.OK
            );
        } catch (DataAccessException e) {
            responseEntity = new ResponseEntity<>(
                    MessageResponse.builder()
                            .message("Error al buscar la Usuario.")
                            .datos(null)
                            .build(),
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
        return responseEntity;
    }

}
