package com.mcmm.model.dto.plantillaCertificado;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlantillaCertificadoDto {

    private Long id;

    @NotBlank(message = "El nombre de la plantilla es obligatorio.")
    @Size(max = 254, message = "El nombre no debe exceder 254 caracteres.")
    private String nombre;

    private String configuracionJson;

    private Boolean estado;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
