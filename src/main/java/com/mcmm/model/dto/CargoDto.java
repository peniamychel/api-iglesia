package com.mcmm.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CargoDto {

    private Long id;

    @NotNull(message = "Requiere el rolCargoId.")
    private Long rolCargoId;

    @NotNull(message = "Requiere el iglesiaId.")
    private Long iglesiaId;

    @NotNull(message = "Requiere el miembroId.")
    private Long idMiembro;

    private String detalle;
    private Date fechaInicio;
    private Date fechaFin;

    private Boolean estado;

    private String uriActaAsignacion;
    private String uriActaDeslindacion;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Datos embebidos para el endpoint mis-colaboradores (evita múltiples llamadas desde el frontend)
    private MiembroInfo miembro;
    private RolCargoInfo rolCargo;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MiembroInfo {
        private Long id;
        private String nombre;
        private String apellido;
        private String ci;
        private String uriFoto;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RolCargoInfo {
        private Long id;
        private String nombre;
        private String tipo;
    }
}
