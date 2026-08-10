package com.mcmm.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload minimo para el badge de notificaciones del sidenav.
 * Reemplaza al forkJoin de 3 llamadas completas que hacia el frontend cada 15s.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificacionBadgeDto {
    private long traspasos;
    private long eventos;
    /** Traspasos que la iglesia solicito y ya fueron aceptados o rechazados. */
    private long respuestas;
    private long total;
}
