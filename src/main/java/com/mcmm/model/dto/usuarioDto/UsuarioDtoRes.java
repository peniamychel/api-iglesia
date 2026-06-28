package com.mcmm.model.dto.usuarioDto;

import com.mcmm.model.dto.RolCargoDto;
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
    private Long miembroId;
    private String iglesiaNombre;
    private Set<RolCargoDto> roles;
    private Set<com.mcmm.model.dto.PrivilegioDto> privilegios;
}
