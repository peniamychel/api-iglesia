package com.mcmm.model.dto.tipoCertificado;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TipoCertificadoDto {
    private Long id;

    @NotBlank
    @Size(max = 254)
    private String nombre;

    private Date fecha;

    private Boolean estado;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}