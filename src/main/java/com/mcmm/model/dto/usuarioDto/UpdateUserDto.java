package com.mcmm.model.dto.usuarioDto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserDto {
    @NotNull
    private Long id;

    @Size(min = 3, max = 20)
    private String username;
    @Email
    private String email;

    private String name;

    private String apellidos;

    private String uriFoto;

    private Boolean estado;

}