package com.mcmm.model.dto.plantillaCertificado;

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
    private String nombre;
    private String configuracionJson;
    private String uriLogo;
    private String uriMarcaAgua;
    private String uriFirma;
    private Boolean estado;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
