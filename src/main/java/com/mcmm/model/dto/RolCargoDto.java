package com.mcmm.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
    @Size(max = 254, message = "El nombre no debe exceder 254 caracteres.")
    private String nombre;

    @Size(max = 254, message = "El nombreRol no debe exceder 254 caracteres.")
    private String nombreRol;

    private Boolean estado;
    private Set<AccionDto> acciones;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
