package com.mcmm.model.dto.participacionEvento;

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
public class ParticipacionEventoDto {
    private Long id;

    private Long certificadoId; // FK to Certificado (nullable)
    @NotNull
    private Long miembroId; // FK to Miembro
    @NotNull
    private Long eventoId; // FK to Evento

    private Boolean estado;
    private Boolean entregado;
    private LocalDateTime fechaEntrega;
    private String numeroLibro;
    private String numeroFolio;
    private Long entregadoPorId;
    private String codigoUnico;
    private String tokenVerificacion;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private com.mcmm.model.dto.MiembroDto.MiembroDto miembroDto;
}