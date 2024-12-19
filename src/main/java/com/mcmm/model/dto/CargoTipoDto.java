package com.mcmm.model.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CargoTipoDto{

    private Long id;
    //private List<CargoDto> cargos;
    private String tipo;
    private String nombre;

    private Boolean estado;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
