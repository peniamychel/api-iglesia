package com.mcmm.controller;

import com.mcmm.model.dao.ParticipacionEventoDao;
import com.mcmm.model.entity.Certificado;
import com.mcmm.model.entity.Evento;
import com.mcmm.model.entity.Iglesia;
import com.mcmm.model.entity.Miembro;
import com.mcmm.model.entity.ParticipacionEvento;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test del controlador de verificacion publica de certificados.
 *
 * Cubre la razon de ser de los dos niveles de acceso (ver
 * [[iglesia-modulos-diseno]] y el hallazgo de enumeracion): el token del QR es
 * inadivinable y muestra todo; el codigo corto impreso tiene solo 32^4
 * combinaciones, asi que va limitado en intentos y con los datos reducidos
 * para que barrer el espacio no sirva de mucho. Tambien cubre el escape de
 * HTML (la pagina se arma concatenando texto que sale de la base) y de que
 * IP se usa para el limite quand hay un proxy de por medio.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CertificadoVerificationControllerTest {

    /** Token de 16 caracteres: cae dentro del rango 14-40 que exige el patron. */
    private static final String TOKEN = "AbCdEfGh12345678";
    /** Codigo corto tal como lo emite el generador: 4 caracteres. */
    private static final String CODIGO_CORTO = "K7M3";

    @Mock private ParticipacionEventoDao participacionEventoDao;
    @Mock private LimitadorVerificacion limitador;
    @Mock private HttpServletRequest request;

    private CertificadoVerificationController controller;

    @BeforeEach
    void setUp() {
        controller = new CertificadoVerificationController();
        // @Autowired es inyeccion por campo: se completa a mano, como hace Spring.
        ReflectionTestUtils.setField(controller, "participacionEventoDao", participacionEventoDao);
        ReflectionTestUtils.setField(controller, "limitador", limitador);
        // Un HttpServletRequest real siempre trae una IP; sin este stub el mock
        // devuelve null y anyString() no matchea null, asi que el limitador caia
        // en su valor por defecto (false) en vez del "true" configurado abajo.
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        when(limitador.permitir(any())).thenReturn(true);
    }

    private ParticipacionEvento participacionCompleta(boolean entregado) {
        Miembro miembro = new Miembro();
        miembro.setNombre("Ronal");
        miembro.setApellido("Morales");

        Iglesia iglesia = new Iglesia();
        iglesia.setNombre("Palmar");

        Evento evento = new Evento();
        evento.setNombre("Bautismo 2026");
        evento.setIglesia(iglesia);

        ParticipacionEvento part = new ParticipacionEvento();
        part.setCodigoUnico(CODIGO_CORTO);
        part.setTokenVerificacion(TOKEN);
        part.setMiembro(miembro);
        part.setEvento(evento);
        part.setEntregado(entregado);
        part.setCreatedAt(LocalDateTime.of(2026, 7, 20, 10, 0));
        if (entregado) {
            part.setFechaEntrega(LocalDateTime.of(2026, 7, 21, 15, 30));
        }
        return part;
    }

    // ───────────────────────── Via token (QR) ─────────────────────────

    @Test
    @DisplayName("Token valido y entregado: muestra los datos completos y el estado Entregado")
    void verificar_porTokenEntregado_muestraTodo() {
        when(participacionEventoDao.findByTokenVerificacionWithRelations(TOKEN))
                .thenReturn(Optional.of(participacionCompleta(true)));

        String html = controller.verificarCertificado(TOKEN, request);

        assertThat(html).contains("Entregado y Válido");
        assertThat(html).contains("Ronal Morales");
        assertThat(html).contains("Palmar");
        assertThat(html).contains("Bautismo 2026");
        // La via token no tiene limite de intentos: nunca deberia consultarse.
        verify(limitador, never()).permitir(anyString());
    }

    @Test
    @DisplayName("Token valido sin entregar: estado 'Entrega Pendiente'")
    void verificar_porTokenSinEntregar_muestraPendiente() {
        when(participacionEventoDao.findByTokenVerificacionWithRelations(TOKEN))
                .thenReturn(Optional.of(participacionCompleta(false)));

        String html = controller.verificarCertificado(TOKEN, request);

        assertThat(html).contains("Entrega Pendiente");
    }

    @Test
    @DisplayName("Token inexistente: 'Código no Válido', sin consultar por codigo corto")
    void verificar_porTokenInexistente_muestraNoValido() {
        when(participacionEventoDao.findByTokenVerificacionWithRelations(TOKEN))
                .thenReturn(Optional.empty());

        String html = controller.verificarCertificado(TOKEN, request);

        assertThat(html).contains("Código no Válido");
        verify(participacionEventoDao, never()).findByCodigoUnicoWithRelations(anyString());
    }

    // ───────────────────────── Via codigo corto ─────────────────────────

    @Test
    @DisplayName("Codigo corto valido: vista reducida, sin iglesia ni evento, nombre abreviado")
    void verificar_porCodigoCorto_muestraVistaReducida() {
        // Con certificado propio (motivo explicito) para aislar esta prueba del
        // caso de abajo: sin certificado, el motivo de repuesto SI menciona el
        // evento, y eso es un escape aparte, no lo que se prueba aqui.
        ParticipacionEvento part = participacionCompleta(true);
        Certificado certificado = new Certificado();
        certificado.setMotivoCertificado("Certificado de Bautismo");
        part.setCertificado(certificado);
        when(participacionEventoDao.findByCodigoUnicoWithRelations(CODIGO_CORTO))
                .thenReturn(Optional.of(part));

        String html = controller.verificarCertificado(CODIGO_CORTO, request);

        assertThat(html).contains("Ronal M."); // nombre + inicial del apellido
        assertThat(html).doesNotContain("Ronal Morales"); // nombre completo, NO
        assertThat(html).doesNotContain("Palmar"); // iglesia, NO
        assertThat(html).doesNotContain("Bautismo 2026"); // evento, NO
        assertThat(html).contains("Vista reducida");
    }

    @Test
    @DisplayName("HALLAZGO: sin certificado asociado, el motivo de repuesto filtra el nombre del evento")
    void verificar_porCodigoCortoSinCertificado_elMotivoDeRepuestoMencionaElEvento() {
        // Documenta un comportamiento real, no lo corrige: si la participacion
        // todavia no tiene certificado, el motivo cae a "Participación en el
        // evento: {nombre}", y esa fila SI se muestra en la vista reducida.
        // La fila explicita "Evento" sigue oculta, pero el dato se filtra igual
        // por esta otra via. No deberia ocurrir en el uso normal (se escanea el
        // QR de un certificado ya generado), pero el codigo/token de una
        // participacion sin certificado tambien son validos para consultar.
        when(participacionEventoDao.findByCodigoUnicoWithRelations(CODIGO_CORTO))
                .thenReturn(Optional.of(participacionCompleta(true)));

        String html = controller.verificarCertificado(CODIGO_CORTO, request);

        assertThat(html).contains("Bautismo 2026");
        assertThat(html).doesNotContain("<span class=\"detail-label\">Evento</span>");
    }

    @Test
    @DisplayName("Codigo corto en minusculas: se busca en mayusculas")
    void verificar_codigoCortoMinusculas_buscaEnMayusculas() {
        when(participacionEventoDao.findByCodigoUnicoWithRelations(CODIGO_CORTO))
                .thenReturn(Optional.of(participacionCompleta(true)));

        controller.verificarCertificado(CODIGO_CORTO.toLowerCase(), request);

        verify(participacionEventoDao).findByCodigoUnicoWithRelations(CODIGO_CORTO);
    }

    @Test
    @DisplayName("Codigo corto sin cupo: 'Demasiados intentos' y no llega a consultar la base")
    void verificar_sinCupoDeIntentos_bloqueaSinConsultarLaBase() {
        when(limitador.permitir(anyString())).thenReturn(false);

        String html = controller.verificarCertificado(CODIGO_CORTO, request);

        assertThat(html).contains("Demasiados intentos");
        verify(participacionEventoDao, never()).findByCodigoUnicoWithRelations(anyString());
    }

    @Test
    @DisplayName("Token del QR: nunca se limita, aunque el limitador este agotado")
    void verificar_porToken_ignoraElLimitadorAunqueEsteAgotado() {
        when(limitador.permitir(anyString())).thenReturn(false);
        when(participacionEventoDao.findByTokenVerificacionWithRelations(TOKEN))
                .thenReturn(Optional.of(participacionCompleta(true)));

        String html = controller.verificarCertificado(TOKEN, request);

        assertThat(html).doesNotContain("Demasiados intentos");
        assertThat(html).contains("Entregado y Válido");
    }

    // ───────────────────────── IP para el limitador ─────────────────────────

    @Test
    @DisplayName("Con X-Forwarded-For: usa la primera IP de la cabecera")
    void verificar_conProxy_usaLaPrimeraIpDeXForwardedFor() {
        when(request.getHeader("X-Forwarded-For")).thenReturn("203.0.113.5, 10.0.0.1");
        when(participacionEventoDao.findByCodigoUnicoWithRelations(anyString()))
                .thenReturn(Optional.empty());

        controller.verificarCertificado(CODIGO_CORTO, request);

        ArgumentCaptor<String> ipCaptor = ArgumentCaptor.forClass(String.class);
        verify(limitador).permitir(ipCaptor.capture());
        assertThat(ipCaptor.getValue()).isEqualTo("203.0.113.5");
    }

    @Test
    @DisplayName("Sin proxy: usa la IP directa de la conexion")
    void verificar_sinProxy_usaLaIpDirecta() {
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("192.168.1.10");
        when(participacionEventoDao.findByCodigoUnicoWithRelations(anyString()))
                .thenReturn(Optional.empty());

        controller.verificarCertificado(CODIGO_CORTO, request);

        ArgumentCaptor<String> ipCaptor = ArgumentCaptor.forClass(String.class);
        verify(limitador).permitir(ipCaptor.capture());
        assertThat(ipCaptor.getValue()).isEqualTo("192.168.1.10");
    }

    // ───────────────────────── Escape de HTML ─────────────────────────

    @Test
    @DisplayName("Nombre con etiquetas HTML: se escapa, no se inyecta en la pagina")
    void verificar_nombreConEtiquetas_seEscapaEnElHtml() {
        ParticipacionEvento part = participacionCompleta(true);
        part.getMiembro().setNombre("<script>alert(1)</script>");
        when(participacionEventoDao.findByTokenVerificacionWithRelations(TOKEN))
                .thenReturn(Optional.of(part));

        String html = controller.verificarCertificado(TOKEN, request);

        assertThat(html).doesNotContain("<script>alert(1)</script>");
        assertThat(html).contains("&lt;script&gt;alert(1)&lt;/script&gt;");
    }

    @Test
    @DisplayName("Codigo no encontrado con comillas: el mensaje de error tambien escapa")
    void verificar_codigoInexistenteConComillas_escapaElMensaje() {
        String codigoConComillas = "\"><img src=x>";
        when(participacionEventoDao.findByCodigoUnicoWithRelations(anyString()))
                .thenReturn(Optional.empty());

        String html = controller.verificarCertificado(codigoConComillas, request);

        assertThat(html).doesNotContain("\"><img src=x>");
    }

    // ───────────────────────── Certificado con motivo propio ─────────────────────────

    @Test
    @DisplayName("Con certificado asociado: el motivo mostrado es el del certificado, no el generico")
    void verificar_conCertificadoAsociado_usaSuMotivo() {
        ParticipacionEvento part = participacionCompleta(true);
        Certificado certificado = new Certificado();
        certificado.setMotivoCertificado("Certificado de Bautismo");
        part.setCertificado(certificado);
        when(participacionEventoDao.findByTokenVerificacionWithRelations(TOKEN))
                .thenReturn(Optional.of(part));

        String html = controller.verificarCertificado(TOKEN, request);

        assertThat(html).contains("Certificado de Bautismo");
        assertThat(html).doesNotContain("Participación en el evento");
    }
}
