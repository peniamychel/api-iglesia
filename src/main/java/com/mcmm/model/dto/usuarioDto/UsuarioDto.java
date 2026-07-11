package com.mcmm.model.dto.usuarioDto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
    @Email(message = "El email debe tener un formato valido")
    @NotBlank(message = "El email es obligatorio")
    @Size(max = 254, message = "El email no debe exceder 254 caracteres")
    private String email;
    @NotBlank(message = "El username es obligatorio")
    @Size(min = 3, max = 20, message = "El username debe tener entre 3 y 20 caracteres")
    private String username;
    @Size(max = 100, message = "El nombre no debe exceder 100 caracteres")
    private String name;
    @Size(max = 100, message = "Los apellidos no deben exceder 100 caracteres")
    private String apellidos;
    private String uriFoto;
    private Boolean estado;
    private Long miembroId;
    @NotBlank(message = "El password es obligatorio")
    @Size(min = 6, max = 40, message = "El password debe tener entre 6 y 40 caracteres")
    private String password;
    private Set<String> roles;
}
