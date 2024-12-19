package com.mcmm.model.dto;

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
    private Long tipoCargoId;
    private Long iglesiaId;
    private Long idMiembro;
    private String detalle;
    private Date fechaInicio;
    private Date fechaFin;

    private Boolean estado;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
