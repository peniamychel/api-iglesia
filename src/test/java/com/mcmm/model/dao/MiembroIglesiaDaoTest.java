package com.mcmm.model.dao;

import com.mcmm.model.entity.Iglesia;
import com.mcmm.model.entity.Miembro;
import com.mcmm.model.entity.MiembroIglesia;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test de repositorio (nivel "Datos"): las @Query JPQL de MiembroIglesiaDao
 * contra una base H2 real, no contra mocks. Es el complemento de
 * MiembroIglesiaImplTest (que mockea el DAO): alli se prueba la logica de
 * negocio de las transiciones, aqui se prueba que las consultas mismas hagan
 * lo que su nombre promete.
 *
 * Vale la pena como nivel aparte porque estas consultas tienen logica sutil
 * que un mock no puede detectar si se rompe: el OR entre iglesia/iglesiaDestino
 * en los traspasos pendientes, el "solo origen" (sin destino) en las
 * respuestas sin ver, y el IS NULL OR false de respuestaVista para las filas
 * viejas que no tenian esa columna.
 *
 * "test" activa application-test.properties (ver ese archivo): sin el, Spring
 * intenta arrancar con el datasource de MariaDB de src/main/resources antes de
 * que @DataJpaTest pueda reemplazarlo por H2.
 */
@DataJpaTest
@ActiveProfiles("test")
class MiembroIglesiaDaoTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MiembroIglesiaDao dao;

    private Iglesia nuevaIglesia(String nombre) {
        Iglesia iglesia = new Iglesia();
        iglesia.setNombre(nombre);
        iglesia.setEstado(true);
        return entityManager.persistAndFlush(iglesia);
    }

    private Miembro nuevoMiembro(String nombre, String ci) {
        Miembro miembro = new Miembro();
        miembro.setNombre(nombre);
        miembro.setApellido("Apellido");
        miembro.setCi(ci);
        miembro.setEstado(true);
        return entityManager.persistAndFlush(miembro);
    }

    private MiembroIglesia nuevaAsignacion(Miembro miembro, Iglesia iglesia, boolean activa) {
        MiembroIglesia mi = new MiembroIglesia();
        mi.setMiembro(miembro);
        mi.setIglesia(iglesia);
        mi.setEstado(activa);
        return entityManager.persistAndFlush(mi);
    }

    private MiembroIglesia nuevaSolicitudTraspaso(Miembro miembro, Iglesia origen, Iglesia destino, String estadoTraspaso) {
        MiembroIglesia mi = new MiembroIglesia();
        mi.setMiembro(miembro);
        mi.setIglesia(origen);
        mi.setIglesiaDestino(destino);
        mi.setEstadoTraspaso(estadoTraspaso);
        mi.setEstado(estadoTraspaso.equals("PENDIENTE"));
        return entityManager.persistAndFlush(mi);
    }

    // ───────────────────────── findActiveByMiembroId ─────────────────────────

    @Test
    @DisplayName("findActiveByMiembroId: solo trae la asignacion activa del miembro")
    void findActiveByMiembroId_traeSoloLaActiva() {
        Iglesia palmar = nuevaIglesia("Palmar");
        Iglesia libertad = nuevaIglesia("Libertad");
        Miembro miembro = nuevoMiembro("Carlos", "111");
        nuevaAsignacion(miembro, palmar, false); // historica, inactiva
        MiembroIglesia activa = nuevaAsignacion(miembro, libertad, true);

        Optional<MiembroIglesia> resultado = dao.findActiveByMiembroId(miembro.getId());

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getId()).isEqualTo(activa.getId());
        assertThat(resultado.get().getIglesia().getNombre()).isEqualTo("Libertad");
    }

    @Test
    @DisplayName("findActiveByMiembroId: sin asignacion activa, vacio")
    void findActiveByMiembroId_sinActiva_devuelveVacio() {
        Iglesia palmar = nuevaIglesia("Palmar");
        Miembro miembro = nuevoMiembro("Carlos", "112");
        nuevaAsignacion(miembro, palmar, false);

        assertThat(dao.findActiveByMiembroId(miembro.getId())).isEmpty();
    }

    // ───────────────────────── existsByMiembroIdAndEstadoTraspasoPending ─────────────────────────

    @Test
    @DisplayName("existsByMiembroIdAndEstadoTraspasoPending: true solo si tiene un PENDIENTE")
    void existsByMiembroIdAndEstadoTraspasoPending_detectaElPendiente() {
        Iglesia palmar = nuevaIglesia("Palmar");
        Iglesia libertad = nuevaIglesia("Libertad");
        Miembro conPendiente = nuevoMiembro("Ana", "201");
        Miembro sinPendiente = nuevoMiembro("Beto", "202");

        nuevaSolicitudTraspaso(conPendiente, palmar, libertad, "PENDIENTE");
        nuevaSolicitudTraspaso(sinPendiente, palmar, libertad, "ACEPTADO");

        assertThat(dao.existsByMiembroIdAndEstadoTraspasoPending(conPendiente.getId())).isTrue();
        assertThat(dao.existsByMiembroIdAndEstadoTraspasoPending(sinPendiente.getId())).isFalse();
    }

    // ───────────────────────── findPendingTransfersForChurch / count ─────────────────────────

    @Test
    @DisplayName("findPendingTransfersForChurch: trae los pendientes donde la iglesia es ORIGEN")
    void findPendingTransfersForChurch_incluyeComoOrigen() {
        Iglesia palmar = nuevaIglesia("Palmar");
        Iglesia libertad = nuevaIglesia("Libertad");
        Miembro miembro = nuevoMiembro("Carlos", "301");
        nuevaSolicitudTraspaso(miembro, palmar, libertad, "PENDIENTE");

        List<MiembroIglesia> resultado = dao.findPendingTransfersForChurch(palmar.getId());

        assertThat(resultado).hasSize(1);
        assertThat(dao.countPendingTransfersForChurch(palmar.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("findPendingTransfersForChurch: tambien trae los pendientes donde la iglesia es DESTINO")
    void findPendingTransfersForChurch_incluyeComoDestino() {
        Iglesia palmar = nuevaIglesia("Palmar");
        Iglesia libertad = nuevaIglesia("Libertad");
        Miembro miembro = nuevoMiembro("Carlos", "302");
        nuevaSolicitudTraspaso(miembro, palmar, libertad, "PENDIENTE");

        // La misma solicitud debe aparecer tambien consultando por el destino.
        List<MiembroIglesia> resultado = dao.findPendingTransfersForChurch(libertad.getId());

        assertThat(resultado).hasSize(1);
        assertThat(dao.countPendingTransfersForChurch(libertad.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("findPendingTransfersForChurch: no trae solicitudes de una iglesia ajena")
    void findPendingTransfersForChurch_ignoraIglesiaAjena() {
        Iglesia palmar = nuevaIglesia("Palmar");
        Iglesia libertad = nuevaIglesia("Libertad");
        Iglesia ajena = nuevaIglesia("Sacaba");
        Miembro miembro = nuevoMiembro("Carlos", "303");
        nuevaSolicitudTraspaso(miembro, palmar, libertad, "PENDIENTE");

        assertThat(dao.findPendingTransfersForChurch(ajena.getId())).isEmpty();
        assertThat(dao.countPendingTransfersForChurch(ajena.getId())).isZero();
    }

    @Test
    @DisplayName("findPendingTransfersForChurch: no trae solicitudes ya resueltas")
    void findPendingTransfersForChurch_ignoraResueltas() {
        Iglesia palmar = nuevaIglesia("Palmar");
        Iglesia libertad = nuevaIglesia("Libertad");
        Miembro miembro = nuevoMiembro("Carlos", "304");
        nuevaSolicitudTraspaso(miembro, palmar, libertad, "ACEPTADO");

        assertThat(dao.findPendingTransfersForChurch(palmar.getId())).isEmpty();
    }

    @Test
    @DisplayName("findAllPendingTransfers / countAllPendingTransfers: cuentan todas, sin filtrar por iglesia")
    void findAllPendingTransfers_cuentaGlobalmente() {
        Iglesia palmar = nuevaIglesia("Palmar");
        Iglesia libertad = nuevaIglesia("Libertad");
        Iglesia sacaba = nuevaIglesia("Sacaba");
        nuevaSolicitudTraspaso(nuevoMiembro("A", "401"), palmar, libertad, "PENDIENTE");
        nuevaSolicitudTraspaso(nuevoMiembro("B", "402"), sacaba, libertad, "PENDIENTE");
        nuevaSolicitudTraspaso(nuevoMiembro("C", "403"), palmar, sacaba, "ACEPTADO");

        assertThat(dao.findAllPendingTransfers()).hasSize(2);
        assertThat(dao.countAllPendingTransfers()).isEqualTo(2);
    }

    // ───────────────────────── Respuestas sin ver ─────────────────────────

    @Test
    @DisplayName("findRespuestasSinVerParaOrigen: solo mira la iglesia de ORIGEN, no el destino")
    void findRespuestasSinVerParaOrigen_soloMiraElOrigen() {
        Iglesia palmar = nuevaIglesia("Palmar");
        Iglesia libertad = nuevaIglesia("Libertad");
        Miembro miembro = nuevoMiembro("Carlos", "501");
        MiembroIglesia solicitud = nuevaSolicitudTraspaso(miembro, palmar, libertad, "ACEPTADO");
        solicitud.setRespuestaVista(false);
        entityManager.persistAndFlush(solicitud);

        // Palmar solicito el traspaso: le corresponde el aviso.
        assertThat(dao.findRespuestasSinVerParaOrigen(palmar.getId())).hasSize(1);
        assertThat(dao.countRespuestasSinVerParaOrigen(palmar.getId())).isEqualTo(1);

        // A diferencia de findPendingTransfersForChurch, esta consulta NO
        // considera a la iglesia destino: Libertad no solicito nada, no le
        // corresponde ver el aviso de vuelta.
        assertThat(dao.findRespuestasSinVerParaOrigen(libertad.getId())).isEmpty();
        assertThat(dao.countRespuestasSinVerParaOrigen(libertad.getId())).isZero();
    }

    @Test
    @DisplayName("findRespuestasSinVerParaOrigen: PENDIENTE no cuenta como respuesta")
    void findRespuestasSinVerParaOrigen_ignoraPendientes() {
        Iglesia palmar = nuevaIglesia("Palmar");
        Iglesia libertad = nuevaIglesia("Libertad");
        Miembro miembro = nuevoMiembro("Carlos", "502");
        nuevaSolicitudTraspaso(miembro, palmar, libertad, "PENDIENTE");

        assertThat(dao.findRespuestasSinVerParaOrigen(palmar.getId())).isEmpty();
    }

    @Test
    @DisplayName("findRespuestasSinVerParaOrigen: una vez marcada como vista, deja de aparecer")
    void findRespuestasSinVerParaOrigen_marcadaComoVista_desaparece() {
        Iglesia palmar = nuevaIglesia("Palmar");
        Iglesia libertad = nuevaIglesia("Libertad");
        Miembro miembro = nuevoMiembro("Carlos", "503");
        MiembroIglesia solicitud = nuevaSolicitudTraspaso(miembro, palmar, libertad, "RECHAZADO");
        solicitud.setRespuestaVista(true);
        entityManager.persistAndFlush(solicitud);

        assertThat(dao.findRespuestasSinVerParaOrigen(palmar.getId())).isEmpty();
        assertThat(dao.countRespuestasSinVerParaOrigen(palmar.getId())).isZero();
    }

    @Test
    @DisplayName("findRespuestasSinVerParaOrigen: respuestaVista NULL (filas historicas) tambien se considera sin ver")
    void findRespuestasSinVerParaOrigen_respuestaVistaNula_seConsideraSinVer() {
        Iglesia palmar = nuevaIglesia("Palmar");
        Iglesia libertad = nuevaIglesia("Libertad");
        Miembro miembro = nuevoMiembro("Carlos", "504");
        MiembroIglesia solicitud = nuevaSolicitudTraspaso(miembro, palmar, libertad, "ACEPTADO");
        // No se toca respuestaVista: queda NULL, como los registros anteriores
        // a que la columna existiera.
        entityManager.persistAndFlush(solicitud);

        assertThat(dao.findRespuestasSinVerParaOrigen(palmar.getId())).hasSize(1);
    }

    @Test
    @DisplayName("findTodasLasRespuestasSinVer / count: version global, sin filtrar por iglesia")
    void findTodasLasRespuestasSinVer_cuentaGlobalmente() {
        Iglesia palmar = nuevaIglesia("Palmar");
        Iglesia libertad = nuevaIglesia("Libertad");
        Iglesia sacaba = nuevaIglesia("Sacaba");
        MiembroIglesia s1 = nuevaSolicitudTraspaso(nuevoMiembro("A", "601"), palmar, libertad, "ACEPTADO");
        s1.setRespuestaVista(false);
        entityManager.persistAndFlush(s1);
        MiembroIglesia s2 = nuevaSolicitudTraspaso(nuevoMiembro("B", "602"), sacaba, libertad, "RECHAZADO");
        s2.setRespuestaVista(false);
        entityManager.persistAndFlush(s2);

        assertThat(dao.findTodasLasRespuestasSinVer()).hasSize(2);
        assertThat(dao.countTodasLasRespuestasSinVer()).isEqualTo(2);
    }

    // ───────────────────────── findHistorialByMiembroId ─────────────────────────

    @Test
    @DisplayName("findHistorialByMiembroId: ordena por fecha descendente (la mas reciente primero)")
    void findHistorialByMiembroId_ordenaDescendente() throws InterruptedException {
        Iglesia palmar = nuevaIglesia("Palmar");
        Iglesia libertad = nuevaIglesia("Libertad");
        Miembro miembro = nuevoMiembro("Carlos", "701");

        MiembroIglesia antigua = new MiembroIglesia();
        antigua.setMiembro(miembro);
        antigua.setIglesia(palmar);
        antigua.setFecha(new java.util.Date(1000));
        entityManager.persistAndFlush(antigua);

        MiembroIglesia reciente = new MiembroIglesia();
        reciente.setMiembro(miembro);
        reciente.setIglesia(libertad);
        reciente.setFecha(new java.util.Date(2000));
        entityManager.persistAndFlush(reciente);

        List<MiembroIglesia> historial = dao.findHistorialByMiembroId(miembro.getId());

        assertThat(historial).hasSize(2);
        assertThat(historial.get(0).getId()).isEqualTo(reciente.getId());
        assertThat(historial.get(1).getId()).isEqualTo(antigua.getId());
    }
}
