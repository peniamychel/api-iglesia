package com.mcmm.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RolCargoDto {

    private Long id;

    private String tipo;

    @NotBlank(message = "El nombre es obligatorio.")
    private String nombre;

    private String nombreRol;

    private Boolean estado;
    private Set<PrivilegioDto> privilegios;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
