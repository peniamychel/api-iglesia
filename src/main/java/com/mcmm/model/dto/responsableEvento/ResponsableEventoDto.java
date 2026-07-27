package com.mcmm.model.dto.responsableEvento;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResponsableEventoDto {
    private Long id;

    @NotNull
    private Long eventoId; // FK to Evento
    @NotNull
    private Long cargoId; // FK to Cargo

    private Boolean estado;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private String nombreCompleto;
    private String nombreCargo;
}