package com.mcmm.model.dto.usuarioDto;

import com.mcmm.model.entity.Rol;
import lombok.*;

import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioDtoRes {
    private Long id;
    private String email;
    private String username;
    private String name;
    private String apellidos;
    private String uriFoto;
    private Boolean estado;
    private String password;
    private Set<Rol> roles;
}
