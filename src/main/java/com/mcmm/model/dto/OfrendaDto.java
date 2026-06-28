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
public class OfrendaDto {
    private Long id;

    @NotNull(message = "Requiere el iglesiaId.")
    private Long iglesiaId;
    private String iglesiaNombre;

    @NotNull(message = "Requiere el tipoMovimiento.")
    private String tipoMovimiento;

    @NotNull(message = "Requiere el monto.")
    private Double monto;

    @NotNull(message = "Requiere la fecha de recaudación.")
    private Date fechaRecaudacion;

    private String conceptoDetalle;

    private Long usuarioTesoreroId;
    private String usuarioTesoreroUsername;

    private LocalDateTime fechaRegistro;
}
