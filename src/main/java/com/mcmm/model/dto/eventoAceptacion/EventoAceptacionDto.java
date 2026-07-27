package com.mcmm.model.dto.eventoAceptacion;

import lombok.*;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EventoAceptacionDto {
    private Long id;
    private Long eventoId;
    private Long iglesiaId;
    private String estado;
    private LocalDateTime updatedAt;
}
