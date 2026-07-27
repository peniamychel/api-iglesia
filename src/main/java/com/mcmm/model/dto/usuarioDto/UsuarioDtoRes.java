package com.mcmm.model.dto.usuarioDto;

import com.mcmm.model.dto.RolCargoDto;
import com.mcmm.model.dto.AccionDto;
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
    private Boolean esAdmin;
    private Long miembroId;
    private String iglesiaNombre;
    private Set<RolCargoDto> roles;
    private Set<AccionDto> acciones;
}
