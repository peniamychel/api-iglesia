package com.mcmm.model.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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
    @Pattern(regexp = "INGRESO|EGRESO", message = "El tipo de movimiento debe ser INGRESO o EGRESO.")
    private String tipoMovimiento;

    @NotNull(message = "Requiere el monto.")
    @DecimalMin(value = "0.01", message = "El monto debe ser mayor que cero.")
    private Double monto;

    @NotNull(message = "Requiere la fecha de recaudación.")
    private Date fechaRecaudacion;

    @Size(max = 500, message = "El concepto detalle no debe exceder 500 caracteres.")
    private String conceptoDetalle;

    private Long usuarioTesoreroId;
    private String usuarioTesoreroUsername;

    private LocalDateTime fechaRegistro;

    // Campos de solo respuesta (auditoria / estado)
    private Boolean estado;
    private LocalDateTime updatedAt;
    private String usuarioModificacionUsername;
}
