package com.mcmm.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
    @Size(max = 254, message = "El nombre no debe exceder 254 caracteres.")
    private String nombre;

    @Size(max = 500, message = "La descripcion no debe exceder 500 caracteres.")
    private String descripcion;

    @NotNull(message = "La cantidad es obligatoria.")
    @Min(value = 1, message = "La cantidad mínima debe ser 1.")
    private Integer cantidad;

    @Size(max = 20, message = "El estado de conservacion no debe exceder 20 caracteres.")
    private String estadoConservacion;

    private Double valorEstimado;

    private Date fechaAdquisicion;

    @Size(max = 50, message = "El codigo no debe exceder 50 caracteres.")
    private String codigo;

    private String uriFoto;

    @NotNull(message = "La iglesia es obligatoria.")
    private Long iglesiaId;

    private String iglesiaNombre;
}
