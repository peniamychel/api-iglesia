package com.mcmm.service.impl;

import com.mcmm.model.dao.EventoAceptacionDao;
import com.mcmm.model.dao.EventoDao;
import com.mcmm.model.dao.MiembroIglesiaDao;
import com.mcmm.model.dto.NotificacionBadgeDto;
import com.mcmm.service.INotificacion;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Conteo liviano de notificaciones pendientes para el badge del sidenav.
 * Replica la logica que antes calculaba el frontend con un forkJoin de 3
 * llamadas completas (traspasos, todos los eventos, todas las decisiones),
 * pero devolviendo solo los numeros agregados.
 */
@Service
public class NotificacionImpl implements INotificacion {

    private final MiembroIglesiaDao miembroIglesiaDao;
    private final EventoDao eventoDao;
    private final EventoAceptacionDao eventoAceptacionDao;

    public NotificacionImpl(MiembroIglesiaDao miembroIglesiaDao, EventoDao eventoDao,
                             EventoAceptacionDao eventoAceptacionDao) {
        this.miembroIglesiaDao = miembroIglesiaDao;
        this.eventoDao = eventoDao;
        this.eventoAceptacionDao = eventoAceptacionDao;
    }

    @Override
    @Transactional(readOnly = true)
    public NotificacionBadgeDto getBadge(Long iglesiaId) {
        boolean tieneIglesia = iglesiaId != null && iglesiaId != 0;

        long traspasos = tieneIglesia
                ? miembroIglesiaDao.countPendingTransfersForChurch(iglesiaId)
                : miembroIglesiaDao.countAllPendingTransfers();

        long eventos = 0;
        if (tieneIglesia) {
            List<Long> eventosHabilitados = eventoDao.findIdsEventosHabilitadosParaIglesia(iglesiaId);
            if (!eventosHabilitados.isEmpty()) {
                Set<Long> decididos = new HashSet<>(
                        eventoAceptacionDao.findEventoIdsDecididos(iglesiaId, eventosHabilitados));
                eventos = eventosHabilitados.stream().filter(id -> !decididos.contains(id)).count();
            }
        }

        return NotificacionBadgeDto.builder()
                .traspasos(traspasos)
                .eventos(eventos)
                .total(traspasos + eventos)
                .build();
    }
}
