package com.mcmm.service.impl;

import com.mcmm.model.dao.EventoAceptacionDao;
import com.mcmm.model.dao.EventoDao;
import com.mcmm.model.dao.MiembroIglesiaDao;
import com.mcmm.model.dto.NotificacionBadgeDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test unitario de la logica de conteo del badge de notificaciones, que replica
 * lo que antes calculaba el frontend con un forkJoin de 3 llamadas completas.
 */
@ExtendWith(MockitoExtension.class)
class NotificacionImplTest {

    @Mock
    private MiembroIglesiaDao miembroIglesiaDao;
    @Mock
    private EventoDao eventoDao;
    @Mock
    private EventoAceptacionDao eventoAceptacionDao;

    @InjectMocks
    private NotificacionImpl service;

    @Test
    void getBadge_sinIglesia_cuentaTraspasosGlobalesYCeroEventos() {
        when(miembroIglesiaDao.countAllPendingTransfers()).thenReturn(4L);

        NotificacionBadgeDto badge = service.getBadge(null);

        assertThat(badge.getTraspasos()).isEqualTo(4);
        assertThat(badge.getEventos()).isEqualTo(0);
        assertThat(badge.getTotal()).isEqualTo(4);
        // Sin iglesia no se consultan eventos.
        verify(eventoDao, never()).findIdsEventosHabilitadosParaIglesia(org.mockito.ArgumentMatchers.anyLong());
    }

    @Test
    void getBadge_conIglesia_soloCuentaEventosHabilitadosSinDecision() {
        long iglesiaId = 37L;
        when(miembroIglesiaDao.countPendingTransfersForChurch(iglesiaId)).thenReturn(1L);
        // 3 eventos habilitados para la iglesia...
        when(eventoDao.findIdsEventosHabilitadosParaIglesia(iglesiaId))
                .thenReturn(Arrays.asList(10L, 11L, 12L));
        // ...pero 2 de ellos ya tienen decision tomada -> solo 1 pendiente.
        when(eventoAceptacionDao.findEventoIdsDecididos(iglesiaId, Arrays.asList(10L, 11L, 12L)))
                .thenReturn(Arrays.asList(10L, 12L));

        NotificacionBadgeDto badge = service.getBadge(iglesiaId);

        assertThat(badge.getTraspasos()).isEqualTo(1);
        assertThat(badge.getEventos()).isEqualTo(1);
        assertThat(badge.getTotal()).isEqualTo(2);
    }

    @Test
    void getBadge_sinEventosHabilitados_noConsultaDecisiones() {
        long iglesiaId = 5L;
        when(miembroIglesiaDao.countPendingTransfersForChurch(iglesiaId)).thenReturn(0L);
        when(eventoDao.findIdsEventosHabilitadosParaIglesia(iglesiaId))
                .thenReturn(Collections.emptyList());

        NotificacionBadgeDto badge = service.getBadge(iglesiaId);

        assertThat(badge.getTraspasos()).isEqualTo(0);
        assertThat(badge.getEventos()).isEqualTo(0);
        assertThat(badge.getTotal()).isEqualTo(0);
        // No debe consultar decisiones si no hay eventos habilitados (evita IN vacio).
        verify(eventoAceptacionDao, never()).findEventoIdsDecididos(
                org.mockito.ArgumentMatchers.anyLong(), org.mockito.ArgumentMatchers.anyList());
    }

    @Test
    void getBadge_iglesiaCero_tratadaComoSinIglesia() {
        when(miembroIglesiaDao.countAllPendingTransfers()).thenReturn(2L);

        NotificacionBadgeDto badge = service.getBadge(0L);

        assertThat(badge.getTraspasos()).isEqualTo(2);
        assertThat(badge.getEventos()).isEqualTo(0);
        assertThat(badge.getTotal()).isEqualTo(2);
    }
}
