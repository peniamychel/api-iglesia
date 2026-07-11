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
public class ServicioDto implements java.io.Serializable {
    private Long id;

    @NotBlank(message = "El codigo es obligatorio.")
    @Size(max = 100, message = "El codigo no debe exceder 100 caracteres.")
    private String codigo;

    @NotBlank(message = "El nombre es obligatorio.")
    @Size(max = 254, message = "El nombre no debe exceder 254 caracteres.")
    private String nombre;

    @Size(max = 500, message = "La descripcion no debe exceder 500 caracteres.")
    private String descripcion;

    @Size(max = 254, message = "El icono no debe exceder 254 caracteres.")
    private String icono;

    @Size(max = 254, message = "La ruta no debe exceder 254 caracteres.")
    private String ruta;

    private Integer orden;
    private Boolean activo;
    private Set<AccionDto> acciones;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
