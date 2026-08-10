package com.mcmm.service.impl;

import com.mcmm.exception.NotFoundExceptionResource;
import com.mcmm.model.dao.CertificadoDao;
import com.mcmm.model.dao.EventoAceptacionDao;
import com.mcmm.model.dao.EventoDao;
import com.mcmm.model.dao.IglesiaDao;
import com.mcmm.model.dao.ParticipacionEventoDao;
import com.mcmm.model.dao.ResponsableEventoDao;
import com.mcmm.model.dao.TipoEventoDao;
import com.mcmm.model.dao.UsuarioDao;
import com.mcmm.model.dto.evento.EventoDto;
import com.mcmm.model.entity.Evento;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Test de update(), centrado en finDeJornada(): la fecha de fin de un evento
 * llega del formulario en 00:00 (solo se pide el dia), asi que un evento de un
 * solo dia empezaba y terminaba en el mismo instante. Se corrige llevandola al
 * final del dia salvo que ya traiga una hora explicita.
 *
 * Tambien cubre la preservacion de "estado" cuando el request no lo trae: es
 * el bug real que motivo reescribir update() para copiar campo por campo en
 * vez de mapear el DTO entero sobre la entidad existente.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EventoImplTest {

    @Mock private EventoDao eventoDao;
    @Mock private TipoEventoDao tipoEventoDao;
    @Mock private IglesiaDao iglesiaDao;
    @Mock private ResponsableEventoDao responsableEventoDao;
    @Mock private UsuarioDao usuarioDao;
    @Mock private CertificadoDao certificadoDao;
    @Mock private ParticipacionEventoDao participacionEventoDao;
    @Mock private EventoAceptacionDao eventoAceptacionDao;

    private EventoImpl service;

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    private EventoImpl newService() {
        return new EventoImpl(eventoDao, tipoEventoDao, iglesiaDao, responsableEventoDao,
                usuarioDao, certificadoDao, participacionEventoDao, eventoAceptacionDao);
    }

    private static Date en(int anio, int mesCeroIndex, int dia, int hora, int minuto, int segundo) {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(anio, mesCeroIndex, dia, hora, minuto, segundo);
        return cal.getTime();
    }

    private static Calendar comoCalendar(Date fecha) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(fecha);
        return cal;
    }

    /** Evento existente minimo para que update() encuentre algo que editar. */
    private Evento eventoExistente(Long id) {
        Evento evento = new Evento();
        evento.setId(id);
        evento.setEstado(true);
        return evento;
    }

    @Test
    @DisplayName("Fecha de fin a medianoche: se lleva a las 23:59:59 del mismo dia")
    void update_fechaFinAMedianoche_seLlevaAFinDeJornada() {
        service = newService();
        Long id = 1L;
        when(eventoDao.findById(id)).thenReturn(Optional.of(eventoExistente(id)));
        when(eventoDao.save(any(Evento.class))).thenAnswer(inv -> inv.getArgument(0));

        EventoDto dto = new EventoDto();
        dto.setId(id);
        dto.setFechaFin(en(2026, Calendar.JULY, 6, 0, 0, 0));

        EventoDto resultado = service.update(dto);

        Calendar fin = comoCalendar(resultado.getFechaFin());
        assertThat(fin.get(Calendar.DAY_OF_MONTH)).isEqualTo(6);
        assertThat(fin.get(Calendar.HOUR_OF_DAY)).isEqualTo(23);
        assertThat(fin.get(Calendar.MINUTE)).isEqualTo(59);
        assertThat(fin.get(Calendar.SECOND)).isEqualTo(59);
    }

    @Test
    @DisplayName("Fecha de fin con hora explicita: se respeta tal cual llega")
    void update_fechaFinConHoraExplicita_noSeToca() {
        service = newService();
        Long id = 2L;
        when(eventoDao.findById(id)).thenReturn(Optional.of(eventoExistente(id)));
        when(eventoDao.save(any(Evento.class))).thenAnswer(inv -> inv.getArgument(0));

        Date finExplicito = en(2026, Calendar.JULY, 6, 18, 30, 0);
        EventoDto dto = new EventoDto();
        dto.setId(id);
        dto.setFechaFin(finExplicito);

        EventoDto resultado = service.update(dto);

        assertThat(resultado.getFechaFin()).isEqualTo(finExplicito);
    }

    @Test
    @DisplayName("Fecha de fin nula: no lanza error y se mantiene nula")
    void update_fechaFinNula_noFalla() {
        service = newService();
        Long id = 3L;
        when(eventoDao.findById(id)).thenReturn(Optional.of(eventoExistente(id)));
        when(eventoDao.save(any(Evento.class))).thenAnswer(inv -> inv.getArgument(0));

        EventoDto dto = new EventoDto();
        dto.setId(id);
        dto.setFechaFin(null);

        EventoDto resultado = service.update(dto);

        assertThat(resultado.getFechaFin()).isNull();
    }

    @Test
    @DisplayName("Evento inexistente: lanza NotFoundExceptionResource y no intenta guardar")
    void update_eventoInexistente_lanzaNotFound() {
        service = newService();
        when(eventoDao.findById(99L)).thenReturn(Optional.empty());

        EventoDto dto = new EventoDto();
        dto.setId(99L);

        assertThatThrownBy(() -> service.update(dto))
                .isInstanceOf(NotFoundExceptionResource.class);
    }

    @Test
    @DisplayName("Estado ausente en el request: conserva el estado que ya tenia el evento")
    void update_sinEstadoEnElRequest_conservaElEstadoExistente() {
        service = newService();
        Long id = 4L;
        Evento existente = eventoExistente(id);
        existente.setEstado(false); // por ejemplo, un evento desactivado
        when(eventoDao.findById(id)).thenReturn(Optional.of(existente));
        when(eventoDao.save(any(Evento.class))).thenAnswer(inv -> inv.getArgument(0));

        EventoDto dto = new EventoDto();
        dto.setId(id);
        dto.setEstado(null); // el formulario de edicion no manda este campo

        EventoDto resultado = service.update(dto);

        assertThat(resultado.getEstado()).isFalse();
    }
}
