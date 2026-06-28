package com.mcmm.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class ActivoDto implements java.io.Serializable {

    private Long id;

    @NotBlank(message = "El nombre del activo es obligatorio.")
    private String nombre;

    private String descripcion;

    @NotNull(message = "La cantidad es obligatoria.")
    @Min(value = 1, message = "La cantidad mínima debe ser 1.")
    private Integer cantidad;

    private String estadoConservacion; // BUENO, REGULAR, MALO

    private Double valorEstimado;

    private Date fechaAdquisicion;

    @NotNull(message = "La iglesia es obligatoria.")
    private Long iglesiaId;

    private String iglesiaNombre;
}
