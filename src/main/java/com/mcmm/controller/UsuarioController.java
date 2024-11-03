package com.mcmm.controller;


import com.mcmm.model.dao.RolDao;
import com.mcmm.model.dao.UsuarioDao;
import com.mcmm.model.dto.UsuarioDto;
import com.mcmm.model.entity.ERole;
import com.mcmm.model.entity.Rol;
import com.mcmm.model.entity.Usuario;
import com.mcmm.model.payload.MessageResponse;
import com.mcmm.service.IUsuario;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
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
    private RolDao rolDao;

    @Autowired
    private IUsuario usuarioService;

    @PostMapping("/create")
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
                .email(usuarioDto.getEmail())
                .password(passwordEncoder.encode(usuarioDto.getPassword()))
                .roles(roles)
                .build();

        usuarioDao.save(usuario);
        return ResponseEntity.ok(usuario);
    }

    @GetMapping("/findall")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> findAll() {
        ResponseEntity<?> responseEntity;
        Iterable<UsuarioDto> usuarioDtos = usuarioService.findAll();
        try {
            responseEntity = new ResponseEntity<>(
                    MessageResponse.builder()
                            .message("Listado de Usuarios")
                            .datos(usuarioDtos)
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
    public String eliminarUsuaior(@RequestParam String id){
        usuarioDao.deleteById(Long.parseLong(id));
        return "Usuario eliminado".concat(id);
    }
}
