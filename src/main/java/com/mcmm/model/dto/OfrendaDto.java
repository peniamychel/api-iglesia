package com.mcmm.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

    private Long iglesiaId;
    private String iglesiaNombre;

    @NotBlank(message = "El tipo de movimiento es obligatorio.")
    @Size(max = 50, message = "El tipo de movimiento no debe exceder 50 caracteres.")
    private String tipoMovimiento;

    @NotNull(message = "Requiere el monto.")
    @Min(value = 0, message = "El monto no puede ser negativo.")
    private Double monto;

    @NotNull(message = "Requiere la fecha de recaudación.")
    private Date fechaRecaudacion;

    @Size(max = 500, message = "El concepto detalle no debe exceder 500 caracteres.")
    private String conceptoDetalle;

    private Long usuarioTesoreroId;
    private String usuarioTesoreroUsername;

    private LocalDateTime fechaRegistro;
}
