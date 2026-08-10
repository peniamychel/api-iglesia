package com.mcmm.service.impl;

import com.mcmm.exception.BadRequestException;
import com.mcmm.exception.InternalServerErrorExceptionResource;
import com.mcmm.model.dao.CertificadoDao;
import com.mcmm.model.dao.EventoDao;
import com.mcmm.model.dao.MiembroDao;
import com.mcmm.model.dao.ParticipacionEventoDao;
import com.mcmm.model.dao.UsuarioDao;
import com.mcmm.model.dto.participacionEvento.ParticipacionEventoDto;
import com.mcmm.model.entity.ParticipacionEvento;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test del generador de codigo de verificacion y token del QR en create().
 *
 * Lo que protege: antes el codigo se generaba en el @PrePersist de la entidad
 * sin comprobar nada contra la base, asi que un choque hacia fallar el INSERT
 * y el usuario recibia un error generico al registrar la participacion. Ver
 * tambien LimitadorVerificacionTest para la otra mitad de la defensa (el
 * codigo corto es enumerable y necesita limite de intentos al verificarlo).
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ParticipacionEventoImplTest {

    /** Sin 0, O, 1, I: el mismo alfabeto que ParticipacionEventoImpl. */
    private static final String PATRON_CODIGO_4 = "^[ABCDEFGHJKLMNPQRSTUVWXYZ23456789]{4}$";
    private static final String PATRON_TOKEN = "^[A-Za-z0-9_-]{16}$";

    @Mock private ParticipacionEventoDao participacionEventoDao;
    @Mock private CertificadoDao certificadoDao;
    @Mock private MiembroDao miembroDao;
    @Mock private EventoDao eventoDao;
    @Mock private UsuarioDao usuarioDao;
    @Mock private ModelMapper modelMapper;

    private ParticipacionEventoImpl service;

    private ParticipacionEventoImpl newService() {
        return new ParticipacionEventoImpl(
                participacionEventoDao, certificadoDao, miembroDao, eventoDao, usuarioDao, modelMapper);
    }

    /** DTO minimo: sin evento ni miembro para no rozar las demas ramas de create(). */
    private ParticipacionEventoDto dtoBasico() {
        ParticipacionEventoDto dto = new ParticipacionEventoDto();
        return dto;
    }

    private void mapearA(ParticipacionEvento entidad) {
        when(modelMapper.map(any(ParticipacionEventoDto.class), org.mockito.ArgumentMatchers.eq(ParticipacionEvento.class)))
                .thenReturn(entidad);
    }

    @Test
    @DisplayName("Sin choques: genera codigo de 4 caracteres sin ambiguedad y token de 16")
    void create_sinColisiones_generaCodigoYTokenValidos() {
        service = newService();
        mapearA(new ParticipacionEvento());
        when(participacionEventoDao.existsByCodigoUnico(anyString())).thenReturn(false);
        when(participacionEventoDao.saveAndFlush(any(ParticipacionEvento.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        service.create(dtoBasico());

        var captor = org.mockito.ArgumentCaptor.forClass(ParticipacionEvento.class);
        verify(participacionEventoDao).saveAndFlush(captor.capture());
        ParticipacionEvento guardada = captor.getValue();

        assertThat(guardada.getCodigoUnico()).matches(PATRON_CODIGO_4);
        assertThat(guardada.getTokenVerificacion()).matches(PATRON_TOKEN);
        // Un solo intento: no hizo falta reintentar.
        verify(participacionEventoDao, times(1)).existsByCodigoUnico(anyString());
    }

    @Test
    @DisplayName("Con un choque: reintenta y guarda con el segundo codigo generado")
    void create_conUnChoque_reintentaYGeneraOtroCodigo() {
        service = newService();
        mapearA(new ParticipacionEvento());
        // El primer candidato choca, el resto no.
        when(participacionEventoDao.existsByCodigoUnico(anyString())).thenReturn(true, false);
        when(participacionEventoDao.saveAndFlush(any(ParticipacionEvento.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        service.create(dtoBasico());

        verify(participacionEventoDao, times(2)).existsByCodigoUnico(anyString());
    }

    @Test
    @DisplayName("Tras 5 choques seguidos, el candidato siguiente ya mide 5 caracteres")
    void create_cincoChoquesSeguidos_amplialaLongitud() {
        service = newService();
        mapearA(new ParticipacionEvento());
        // Los primeros 5 intentos chocan (dispara la ampliacion); el 6to no.
        when(participacionEventoDao.existsByCodigoUnico(anyString()))
                .thenReturn(true, true, true, true, true, false);
        when(participacionEventoDao.saveAndFlush(any(ParticipacionEvento.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        service.create(dtoBasico());

        var captor = org.mockito.ArgumentCaptor.forClass(ParticipacionEvento.class);
        verify(participacionEventoDao).saveAndFlush(captor.capture());
        assertThat(captor.getValue().getCodigoUnico()).hasSize(5);
    }

    @Test
    @DisplayName("Si todo el espacio esta agotado, no inserta y avisa con un mensaje claro")
    void create_espacioAgotado_lanzaErrorSinGuardar() {
        service = newService();
        mapearA(new ParticipacionEvento());
        // Choca siempre: agota los 25 intentos permitidos.
        when(participacionEventoDao.existsByCodigoUnico(anyString())).thenReturn(true);

        assertThatThrownBy(() -> service.create(dtoBasico()))
                .isInstanceOf(InternalServerErrorExceptionResource.class);

        verify(participacionEventoDao, org.mockito.Mockito.never()).saveAndFlush(any());
    }

    @Test
    @DisplayName("Choque en el INSERT (carrera): se traduce a un mensaje que invita a reintentar")
    void create_choqueEnElInsert_lanzaBadRequestConMensajeClaro() {
        service = newService();
        mapearA(new ParticipacionEvento());
        when(participacionEventoDao.existsByCodigoUnico(anyString())).thenReturn(false);
        when(participacionEventoDao.saveAndFlush(any(ParticipacionEvento.class)))
                .thenThrow(new DataIntegrityViolationException("codigo_unico duplicado"));

        assertThatThrownBy(() -> service.create(dtoBasico()))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Vuelva a registrar");
    }

    @Test
    @DisplayName("Si el DTO ya trae codigo y token (edicion/importacion), no los regenera")
    void create_conCodigoYTokenYaPresentes_losRespeta() {
        service = newService();
        ParticipacionEvento entidadMapeada = new ParticipacionEvento();
        entidadMapeada.setCodigoUnico("ABCD");
        entidadMapeada.setTokenVerificacion("token-existente-ya");
        mapearA(entidadMapeada);
        when(participacionEventoDao.saveAndFlush(any(ParticipacionEvento.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        service.create(dtoBasico());

        assertThat(entidadMapeada.getCodigoUnico()).isEqualTo("ABCD");
        assertThat(entidadMapeada.getTokenVerificacion()).isEqualTo("token-existente-ya");
        verify(participacionEventoDao, org.mockito.Mockito.never()).existsByCodigoUnico(anyString());
    }
}
