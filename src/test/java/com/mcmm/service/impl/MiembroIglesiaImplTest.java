package com.mcmm.service.impl;

import com.mcmm.exception.NotFoundExceptionResource;
import com.mcmm.model.dao.IglesiaDao;
import com.mcmm.model.dao.MiembroDao;
import com.mcmm.model.dao.MiembroIglesiaDao;
import com.mcmm.model.dto.MiembroIglesiaDto;
import com.mcmm.model.entity.Iglesia;
import com.mcmm.model.entity.Miembro;
import com.mcmm.model.entity.MiembroIglesia;
import com.mcmm.service.FileStorageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.modelmapper.ModelMapper;

import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test de las transiciones de un traspaso: solicitar, aceptar y rechazar.
 *
 * El caso de rechazarTraspaso() cubre una regresion real: la primera version
 * ponia iglesiaDestino en null y le pegaba " (RECHAZADO)" al motivo, lo que
 * hacia imposible avisarle despues a la iglesia de origen quien rechazo y por
 * que se habia pedido el traspaso (ver [[iglesia-modulos-diseno]]).
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MiembroIglesiaImplTest {

    @Mock private ModelMapper modelMapper;
    @Mock private MiembroIglesiaDao miembroIglesiaDao;
    @Mock private MiembroDao miembroDao;
    @Mock private IglesiaDao iglesiaDao;
    @Mock private FileStorageService fileStorageService;

    private MiembroIglesiaImpl service;

    private MiembroIglesiaImpl newService() {
        MiembroIglesiaImpl s = new MiembroIglesiaImpl(modelMapper, miembroIglesiaDao, miembroDao, iglesiaDao, fileStorageService);
        // convertToDto() necesita mapear la entidad a un DTO no nulo antes de
        // completarlo con los IDs de las relaciones.
        when(modelMapper.map(any(MiembroIglesia.class), eq(MiembroIglesiaDto.class)))
                .thenAnswer(inv -> new MiembroIglesiaDto());
        return s;
    }

    // ───────────────────────── solicitarTraspaso ─────────────────────────

    @Test
    @DisplayName("Miembro con un traspaso ya pendiente: rechaza la nueva solicitud")
    void solicitarTraspaso_conPendienteExistente_lanzaExcepcion() {
        service = newService();
        when(miembroIglesiaDao.existsByMiembroIdAndEstadoTraspasoPending(5L)).thenReturn(true);

        MiembroIglesiaDto dto = new MiembroIglesiaDto();
        dto.setMiembroId(5L);
        dto.setIglesiaDestinoId(2L);

        assertThatThrownBy(() -> service.solicitarTraspaso(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("traspaso pendiente");
    }

    @Test
    @DisplayName("Miembro sin iglesia activa: no se puede solicitar el traspaso")
    void solicitarTraspaso_sinIglesiaActiva_lanzaExcepcion() {
        service = newService();
        when(miembroIglesiaDao.existsByMiembroIdAndEstadoTraspasoPending(5L)).thenReturn(false);
        when(miembroIglesiaDao.findActiveByMiembroId(5L)).thenReturn(Optional.empty());

        MiembroIglesiaDto dto = new MiembroIglesiaDto();
        dto.setMiembroId(5L);
        dto.setIglesiaDestinoId(2L);

        assertThatThrownBy(() -> service.solicitarTraspaso(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no está asignado");
    }

    @Test
    @DisplayName("Solicitud valida: marca PENDIENTE con destino y motivo, sin tocar la iglesia origen")
    void solicitarTraspaso_valida_marcaPendienteConDestinoYMotivo() {
        service = newService();
        MiembroIglesia activa = new MiembroIglesia();
        activa.setId(10L);
        Iglesia origen = new Iglesia();
        origen.setId(1L);
        activa.setIglesia(origen);

        Iglesia destino = new Iglesia();
        destino.setId(2L);

        when(miembroIglesiaDao.existsByMiembroIdAndEstadoTraspasoPending(5L)).thenReturn(false);
        when(miembroIglesiaDao.findActiveByMiembroId(5L)).thenReturn(Optional.of(activa));
        when(iglesiaDao.findById(2L)).thenReturn(Optional.of(destino));
        when(miembroIglesiaDao.save(any(MiembroIglesia.class))).thenAnswer(inv -> inv.getArgument(0));

        MiembroIglesiaDto dto = new MiembroIglesiaDto();
        dto.setMiembroId(5L);
        dto.setIglesiaDestinoId(2L);
        dto.setMotivoTraspaso("Cambio de domicilio");

        service.solicitarTraspaso(dto);

        ArgumentCaptor<MiembroIglesia> captor = ArgumentCaptor.forClass(MiembroIglesia.class);
        verify(miembroIglesiaDao).save(captor.capture());
        MiembroIglesia guardada = captor.getValue();

        assertThat(guardada.getEstadoTraspaso()).isEqualTo("PENDIENTE");
        assertThat(guardada.getIglesiaDestino()).isEqualTo(destino);
        assertThat(guardada.getIglesia()).isEqualTo(origen);
        assertThat(guardada.getMotivoTraspaso()).isEqualTo("Cambio de domicilio");
        assertThat(guardada.getFechaTraspaso()).isNotNull();
    }

    // ───────────────────────── aceptarTraspaso ─────────────────────────

    @Test
    @DisplayName("Solicitud que no esta PENDIENTE: no se puede aceptar")
    void aceptarTraspaso_noPendiente_lanzaExcepcion() {
        service = newService();
        MiembroIglesia solicitud = new MiembroIglesia();
        solicitud.setId(20L);
        solicitud.setEstadoTraspaso("ACEPTADO");
        when(miembroIglesiaDao.findById(20L)).thenReturn(Optional.of(solicitud));

        assertThatThrownBy(() -> service.aceptarTraspaso(20L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no está pendiente");
    }

    @Test
    @DisplayName("Aceptar: desactiva la asignacion vieja y crea una activa en el destino")
    void aceptarTraspaso_pendiente_desactivaLaViejaYCreaLaNueva() {
        service = newService();
        MiembroIglesia solicitud = new MiembroIglesia();
        solicitud.setId(20L);
        solicitud.setEstadoTraspaso("PENDIENTE");
        solicitud.setEstado(true);
        Miembro miembro = new Miembro();
        miembro.setId(5L);
        solicitud.setMiembro(miembro);
        Iglesia destino = new Iglesia();
        destino.setId(2L);
        solicitud.setIglesiaDestino(destino);

        when(miembroIglesiaDao.findById(20L)).thenReturn(Optional.of(solicitud));
        when(miembroIglesiaDao.save(any(MiembroIglesia.class))).thenAnswer(inv -> inv.getArgument(0));

        service.aceptarTraspaso(20L);

        ArgumentCaptor<MiembroIglesia> captor = ArgumentCaptor.forClass(MiembroIglesia.class);
        verify(miembroIglesiaDao, times(2)).save(captor.capture());
        MiembroIglesia viejaDesactivada = captor.getAllValues().get(0);
        MiembroIglesia nuevaActiva = captor.getAllValues().get(1);

        assertThat(viejaDesactivada.getEstado()).isFalse();
        assertThat(viejaDesactivada.getEstadoTraspaso()).isEqualTo("ACEPTADO");
        assertThat(viejaDesactivada.getRespuestaVista()).isFalse();

        assertThat(nuevaActiva.getMiembro()).isEqualTo(miembro);
        assertThat(nuevaActiva.getIglesia()).isEqualTo(destino);
        assertThat(nuevaActiva.getEstado()).isTrue();
    }

    // ───────────────────────── rechazarTraspaso ─────────────────────────

    @Test
    @DisplayName("Solicitud que no esta PENDIENTE: no se puede rechazar")
    void rechazarTraspaso_noPendiente_lanzaExcepcion() {
        service = newService();
        MiembroIglesia solicitud = new MiembroIglesia();
        solicitud.setId(21L);
        solicitud.setEstadoTraspaso("RECHAZADO");
        when(miembroIglesiaDao.findById(21L)).thenReturn(Optional.of(solicitud));

        assertThatThrownBy(() -> service.rechazarTraspaso(21L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no está pendiente");
    }

    @Test
    @DisplayName("Rechazar: conserva la iglesia destino y el motivo original (regresion)")
    void rechazarTraspaso_conservaDestinoYMotivoOriginal() {
        service = newService();
        MiembroIglesia solicitud = new MiembroIglesia();
        solicitud.setId(21L);
        solicitud.setEstadoTraspaso("PENDIENTE");
        Iglesia destino = new Iglesia();
        destino.setId(2L);
        solicitud.setIglesiaDestino(destino);
        solicitud.setMotivoTraspaso("Cambio de domicilio");

        when(miembroIglesiaDao.findById(21L)).thenReturn(Optional.of(solicitud));
        when(miembroIglesiaDao.save(any(MiembroIglesia.class))).thenAnswer(inv -> inv.getArgument(0));

        service.rechazarTraspaso(21L);

        ArgumentCaptor<MiembroIglesia> captor = ArgumentCaptor.forClass(MiembroIglesia.class);
        verify(miembroIglesiaDao).save(captor.capture());
        MiembroIglesia guardada = captor.getValue();

        assertThat(guardada.getEstadoTraspaso()).isEqualTo("RECHAZADO");
        // No debe perderse ninguno de los dos: son los que permiten avisarle
        // despues a la iglesia de origen quien rechazo y por que se pidio.
        assertThat(guardada.getIglesiaDestino()).isEqualTo(destino);
        assertThat(guardada.getMotivoTraspaso()).isEqualTo("Cambio de domicilio");
        assertThat(guardada.getRespuestaVista()).isFalse();
    }

    // ───────────────────────── marcarRespuestaVista ─────────────────────────

    @Test
    @DisplayName("Marcar vista: pasa respuestaVista a true sin tocar el resto")
    void marcarRespuestaVista_marcaComoVista() {
        service = newService();
        MiembroIglesia solicitud = new MiembroIglesia();
        solicitud.setId(22L);
        solicitud.setEstadoTraspaso("ACEPTADO");
        solicitud.setRespuestaVista(false);
        when(miembroIglesiaDao.findById(22L)).thenReturn(Optional.of(solicitud));
        when(miembroIglesiaDao.save(any(MiembroIglesia.class))).thenAnswer(inv -> inv.getArgument(0));

        service.marcarRespuestaVista(22L);

        ArgumentCaptor<MiembroIglesia> captor = ArgumentCaptor.forClass(MiembroIglesia.class);
        verify(miembroIglesiaDao).save(captor.capture());
        assertThat(captor.getValue().getRespuestaVista()).isTrue();
        assertThat(captor.getValue().getEstadoTraspaso()).isEqualTo("ACEPTADO");
    }

    @Test
    @DisplayName("Id inexistente en cualquier transicion: NotFoundExceptionResource")
    void transiciones_idInexistente_lanzaNotFound() {
        service = newService();
        when(miembroIglesiaDao.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.aceptarTraspaso(404L))
                .isInstanceOf(NotFoundExceptionResource.class);
        assertThatThrownBy(() -> service.rechazarTraspaso(404L))
                .isInstanceOf(NotFoundExceptionResource.class);
        assertThatThrownBy(() -> service.marcarRespuestaVista(404L))
                .isInstanceOf(NotFoundExceptionResource.class);
    }
}
