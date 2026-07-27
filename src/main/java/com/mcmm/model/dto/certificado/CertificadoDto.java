package com.mcmm.model.dto.certificado;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @NotNull(message = "El evento es obligatorio")
    private Long eventoId;
    @NotNull(message = "La plantilla de certificado es obligatoria")
    private Long plantillaCertificadoId;

    @NotBlank(message = "El motivo del certificado es obligatorio")
    @Size(max = 254, message = "El motivo no debe exceder 254 caracteres")
    private String motivoCertificado;

    private String uriFoto;

    private Boolean estado;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}