package com.mcmm.model.dto.usuarioDto;

import com.mcmm.model.entity.Rol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
