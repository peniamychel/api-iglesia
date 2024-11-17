package com.mcmm.model.dto.usuarioDto;

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
public class UsuarioDto {

    private Long id;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String username;

    private String name;

    private String apellidos;

    private String uriFoto;

    private Boolean estado;

    @NotBlank
    private String password;

    private Set<String> roles;
}
