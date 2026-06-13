package com.mcmm.model.dto.certificado;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CertificadoDto {
    private Long id;

    private Long eventoId; // FK to Evento
    private Long tipoCertificadoId; // FK to TipoCertificado
    private Long plantillaCertificadoId; // FK to PlantillaCertificado

    @NotBlank
    @Size(max = 254)
    private String motivoCertificado;

    @Size(max = 254)
    private String codigoCertificado;

    private String uriFoto;

    private Boolean estado;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}