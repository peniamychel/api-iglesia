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

    @NotNull(message = "Requiere el tipoCargoId.")
    private Long tipoCargoId;

    @NotNull(message = "Requiere el iglesiaId.")
    private Long iglesiaId;

    @NotNull(message = "Requiere el miembroId.")
    private Long idMiembro;

    private String detalle;
    private Date fechaInicio;
    private Date fechaFin;

    private Boolean estado;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
