package com.mcmm.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AccionDto implements java.io.Serializable {
    private Long id;
    private Long servicioId;
    private String servicioCodigo;

    @NotBlank(message = "El codigo es obligatorio.")
    @Size(max = 100, message = "El codigo no debe exceder 100 caracteres.")
    private String codigo;

    @NotBlank(message = "El nombre es obligatorio.")
    @Size(max = 254, message = "El nombre no debe exceder 254 caracteres.")
    private String nombre;

    @Size(max = 500, message = "La descripcion no debe exceder 500 caracteres.")
    private String descripcion;

    private Boolean activo;

    @Size(max = 254, message = "El authorityCode no debe exceder 254 caracteres.")
    private String authorityCode; // formato SERVICIO:CODIGO (ej: MIEMBROS:VER)

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
