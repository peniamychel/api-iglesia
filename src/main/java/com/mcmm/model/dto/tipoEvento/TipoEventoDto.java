package com.mcmm.model.dto.tipoEvento;

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
public class TipoEventoDto {
    private Long id;

    @NotBlank
    @Size(max = 254)
    private String nombre;

    private Boolean estado;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}