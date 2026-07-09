package com.mcmm.model.dto;

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
    private String codigo;
    private String nombre;
    private String descripcion;
    private String icono;
    private String ruta;
    private Integer orden;
    private Boolean activo;
    private Set<AccionDto> acciones;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
