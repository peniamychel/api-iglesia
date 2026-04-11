package com.mcmm.model.dto;

import jakarta.validation.constraints.NotBlank;
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

    @NotBlank(message = "El nombre es obligatorio.")
    private String nombre;

    private Boolean estado;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
