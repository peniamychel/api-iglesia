package com.mcmm.model.dto.usuarioDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UsuarioResetPasswordDto {
    @NotNull
    private Long id;

    @NotBlank(message = "New password is required")
    @Size(min = 6, max = 40)
    private String newPassword;
}
