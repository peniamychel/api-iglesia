package com.mcmm.model.dto.participacionEvento;

import jakarta.validation.constraints.NotNull;
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
public class ParticipacionEventoDto {
    private Long id;

    private Long certificadoId; // FK to Certificado (nullable)
    @NotNull
    private Long miembroId; // FK to Miembro
    @NotNull
    private Long eventoId; // FK to Evento

    private Date fecha;
    private Boolean estado;
    private Boolean entregado;
    private LocalDateTime fechaEntrega;
    private Long entregadoPorId;
    private String codigoUnico;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}