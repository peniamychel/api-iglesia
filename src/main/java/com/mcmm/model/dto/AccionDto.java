package com.mcmm.model.dto;

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
    private String codigo;
    private String nombre;
    private String descripcion;
    private Boolean activo;
    private String authorityCode; // formato SERVICIO:CODIGO (ej: MIEMBROS:VER)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
