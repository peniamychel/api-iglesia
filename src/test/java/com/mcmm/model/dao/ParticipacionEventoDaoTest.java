package com.mcmm.model.dao;

import com.mcmm.model.entity.Certificado;
import com.mcmm.model.entity.Evento;
import com.mcmm.model.entity.Iglesia;
import com.mcmm.model.entity.Miembro;
import com.mcmm.model.entity.MiembroIglesia;
import com.mcmm.model.entity.ParticipacionEvento;
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
 * Test de repositorio (nivel "Datos") para ParticipacionEventoDao, complemento
 * de ParticipacionEventoImplTest (que mockea el DAO y prueba solo la logica de
 * generacion de codigo/token). Aqui se prueban las @Query JPQL mismas contra
 * H2, en particular findByEventoIglesiaIdWithRelations: su OR EXISTS trae
 * participaciones por dos caminos independientes (evento organizado por la
 * iglesia, o miembro actualmente activo en la iglesia) que un mock no puede
 * distinguir si se rompe.
 *
 * Ver MiembroIglesiaDaoTest para el porque del perfil "test"
 * (application-test.properties): dialecto H2 y quoting global desactivado.
 */
@DataJpaTest
@ActiveProfiles("test")
class ParticipacionEventoDaoTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ParticipacionEventoDao dao;

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

    private void asignarMiembroAIglesia(Miembro miembro, Iglesia iglesia, boolean activa) {
        MiembroIglesia mi = new MiembroIglesia();
        mi.setMiembro(miembro);
        mi.setIglesia(iglesia);
        mi.setEstado(activa);
        entityManager.persistAndFlush(mi);
    }

    private Evento nuevoEvento(String nombre, Iglesia iglesia) {
        Evento evento = new Evento();
        evento.setNombre(nombre);
        evento.setIglesia(iglesia);
        return entityManager.persistAndFlush(evento);
    }

    private ParticipacionEvento nuevaParticipacion(Evento evento, Miembro miembro) {
        ParticipacionEvento p = new ParticipacionEvento();
        p.setEvento(evento);
        p.setMiembro(miembro);
        return entityManager.persistAndFlush(p);
    }

    // ───────────────────────── toggleEstado ─────────────────────────

    @Test
    @DisplayName("toggleEstado: invierte el estado actual")
    void toggleEstado_invierteElEstado() {
        Iglesia iglesia = nuevaIglesia("Palmar");
        Miembro miembro = nuevoMiembro("Carlos", "111");
        Evento evento = nuevoEvento("Retiro", iglesia);
        ParticipacionEvento p = nuevaParticipacion(evento, miembro);
        assertThat(p.getEstado()).isTrue(); // default puesto por @PrePersist

        dao.toggleEstado(p.getId());
        entityManager.clear();

        ParticipacionEvento recargada = entityManager.find(ParticipacionEvento.class, p.getId());
        assertThat(recargada.getEstado()).isFalse();

        dao.toggleEstado(p.getId());
        entityManager.clear();
        assertThat(entityManager.find(ParticipacionEvento.class, p.getId()).getEstado()).isTrue();
    }

    // ───────────────────────── detachCertificado ─────────────────────────

    @Test
    @DisplayName("detachCertificado: quita el certificado de todas las participaciones que lo referencian")
    void detachCertificado_quitaLaReferencia() {
        Iglesia iglesia = nuevaIglesia("Palmar");
        Evento evento = nuevoEvento("Retiro", iglesia);
        Certificado certificado = new Certificado();
        certificado.setEvento(evento);
        entityManager.persistAndFlush(certificado);

        Miembro m1 = nuevoMiembro("Carlos", "201");
        Miembro m2 = nuevoMiembro("Ana", "202");
        ParticipacionEvento p1 = nuevaParticipacion(evento, m1);
        p1.setCertificado(certificado);
        entityManager.persistAndFlush(p1);
        ParticipacionEvento p2 = nuevaParticipacion(evento, m2);
        p2.setCertificado(certificado);
        entityManager.persistAndFlush(p2);

        dao.detachCertificado(certificado.getId());
        entityManager.clear();

        assertThat(entityManager.find(ParticipacionEvento.class, p1.getId()).getCertificado()).isNull();
        assertThat(entityManager.find(ParticipacionEvento.class, p2.getId()).getCertificado()).isNull();
    }

    @Test
    @DisplayName("detachCertificado: no toca participaciones de otro certificado")
    void detachCertificado_ignoraOtroCertificado() {
        Iglesia iglesia = nuevaIglesia("Palmar");
        Evento evento = nuevoEvento("Retiro", iglesia);
        Certificado propio = new Certificado();
        propio.setEvento(evento);
        entityManager.persistAndFlush(propio);
        Certificado ajeno = new Certificado();
        ajeno.setEvento(evento);
        entityManager.persistAndFlush(ajeno);

        Miembro miembro = nuevoMiembro("Carlos", "203");
        ParticipacionEvento p = nuevaParticipacion(evento, miembro);
        p.setCertificado(ajeno);
        entityManager.persistAndFlush(p);

        dao.detachCertificado(propio.getId());
        entityManager.clear();

        assertThat(entityManager.find(ParticipacionEvento.class, p.getId()).getCertificado().getId())
                .isEqualTo(ajeno.getId());
    }

    // ───────────────────────── findByEventoIglesiaIdWithRelations ─────────────────────────

    @Test
    @DisplayName("findByEventoIglesiaIdWithRelations: trae participaciones de eventos organizados por la iglesia")
    void findByEventoIglesiaId_incluyePorEventoDeLaIglesia() {
        Iglesia palmar = nuevaIglesia("Palmar");
        Iglesia libertad = nuevaIglesia("Libertad");
        Evento eventoPalmar = nuevoEvento("Retiro Palmar", palmar);
        // Miembro sin relacion activa con ninguna iglesia: solo debe aparecer via el evento.
        Miembro miembro = nuevoMiembro("Carlos", "301");
        nuevaParticipacion(eventoPalmar, miembro);

        List<ParticipacionEvento> resultado = dao.findByEventoIglesiaIdWithRelations(palmar.getId());

        assertThat(resultado).hasSize(1);
        assertThat(dao.findByEventoIglesiaIdWithRelations(libertad.getId())).isEmpty();
    }

    @Test
    @DisplayName("findByEventoIglesiaIdWithRelations: tambien trae participaciones de un miembro activo en la iglesia, aunque el evento sea de otra")
    void findByEventoIglesiaId_incluyePorMiembroActivoEnLaIglesia() {
        Iglesia organizadora = nuevaIglesia("Sacaba");
        Iglesia miembroIglesia = nuevaIglesia("Libertad");
        Evento evento = nuevoEvento("Congreso Regional", organizadora);
        Miembro miembro = nuevoMiembro("Ana", "302");
        asignarMiembroAIglesia(miembro, miembroIglesia, true);
        nuevaParticipacion(evento, miembro);

        // Libertad no organizo el evento, pero el miembro participante le pertenece.
        List<ParticipacionEvento> resultado = dao.findByEventoIglesiaIdWithRelations(miembroIglesia.getId());

        assertThat(resultado).hasSize(1);
    }

    @Test
    @DisplayName("findByEventoIglesiaIdWithRelations: no cuenta una asignacion de iglesia inactiva")
    void findByEventoIglesiaId_ignoraAsignacionInactiva() {
        Iglesia organizadora = nuevaIglesia("Sacaba");
        Iglesia exIglesia = nuevaIglesia("Libertad");
        Evento evento = nuevoEvento("Congreso Regional", organizadora);
        Miembro miembro = nuevoMiembro("Ana", "303");
        asignarMiembroAIglesia(miembro, exIglesia, false); // ya no esta activo alli
        nuevaParticipacion(evento, miembro);

        assertThat(dao.findByEventoIglesiaIdWithRelations(exIglesia.getId())).isEmpty();
    }

    // ───────────────────────── findAllWithRelations ─────────────────────────

    @Test
    @DisplayName("findAllWithRelations: trae todas las participaciones sin filtrar por iglesia")
    void findAllWithRelations_traeTodas() {
        Iglesia palmar = nuevaIglesia("Palmar");
        Iglesia libertad = nuevaIglesia("Libertad");
        Evento e1 = nuevoEvento("Retiro", palmar);
        Evento e2 = nuevoEvento("Congreso", libertad);
        nuevaParticipacion(e1, nuevoMiembro("Carlos", "401"));
        nuevaParticipacion(e2, nuevoMiembro("Ana", "402"));

        assertThat(dao.findAllWithRelations()).hasSize(2);
    }

    // ───────────────────────── existsByEventoIdAndMiembroId ─────────────────────────

    @Test
    @DisplayName("existsByEventoIdAndMiembroId: true solo para el par evento+miembro exacto")
    void existsByEventoIdAndMiembroId_detectaElParExacto() {
        Iglesia iglesia = nuevaIglesia("Palmar");
        Evento evento = nuevoEvento("Retiro", iglesia);
        Miembro inscrito = nuevoMiembro("Carlos", "501");
        Miembro noInscrito = nuevoMiembro("Beto", "502");
        nuevaParticipacion(evento, inscrito);

        assertThat(dao.existsByEventoIdAndMiembroId(evento.getId(), inscrito.getId())).isTrue();
        assertThat(dao.existsByEventoIdAndMiembroId(evento.getId(), noInscrito.getId())).isFalse();
    }

    // ───────────────────────── existsByEventoId ─────────────────────────

    @Test
    @DisplayName("existsByEventoId: true si el evento tiene al menos un participante")
    void existsByEventoId_detectaAlMenosUnParticipante() {
        Iglesia iglesia = nuevaIglesia("Palmar");
        Evento conParticipantes = nuevoEvento("Retiro", iglesia);
        Evento sinParticipantes = nuevoEvento("Vacio", iglesia);
        nuevaParticipacion(conParticipantes, nuevoMiembro("Carlos", "601"));

        assertThat(dao.existsByEventoId(conParticipantes.getId())).isTrue();
        assertThat(dao.existsByEventoId(sinParticipantes.getId())).isFalse();
    }

    // ───────────────────────── existsByCodigoUnico ─────────────────────────

    @Test
    @DisplayName("existsByCodigoUnico: true solo si el codigo ya fue emitido")
    void existsByCodigoUnico_detectaColision() {
        Iglesia iglesia = nuevaIglesia("Palmar");
        Evento evento = nuevoEvento("Retiro", iglesia);
        ParticipacionEvento p = nuevaParticipacion(evento, nuevoMiembro("Carlos", "701"));

        assertThat(dao.existsByCodigoUnico(p.getCodigoUnico())).isTrue();
        assertThat(dao.existsByCodigoUnico("ZZZZ")).isFalse();
    }

    // ───────────────────────── findByCodigoUnicoWithRelations ─────────────────────────

    @Test
    @DisplayName("findByCodigoUnicoWithRelations: encuentra por el codigo corto exacto")
    void findByCodigoUnicoWithRelations_encuentraPorCodigo() {
        Iglesia iglesia = nuevaIglesia("Palmar");
        Evento evento = nuevoEvento("Retiro", iglesia);
        ParticipacionEvento p = nuevaParticipacion(evento, nuevoMiembro("Carlos", "801"));

        Optional<ParticipacionEvento> resultado = dao.findByCodigoUnicoWithRelations(p.getCodigoUnico());

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getId()).isEqualTo(p.getId());
        assertThat(resultado.get().getEvento().getIglesia().getNombre()).isEqualTo("Palmar");
    }

    @Test
    @DisplayName("findByCodigoUnicoWithRelations: codigo inexistente, vacio")
    void findByCodigoUnicoWithRelations_codigoInexistente_devuelveVacio() {
        assertThat(dao.findByCodigoUnicoWithRelations("NADA")).isEmpty();
    }

    // ───────────────────────── findByTokenVerificacionWithRelations ─────────────────────────

    @Test
    @DisplayName("findByTokenVerificacionWithRelations: encuentra por el token largo exacto")
    void findByTokenVerificacionWithRelations_encuentraPorToken() {
        Iglesia iglesia = nuevaIglesia("Palmar");
        Evento evento = nuevoEvento("Retiro", iglesia);
        ParticipacionEvento p = nuevaParticipacion(evento, nuevoMiembro("Carlos", "901"));

        Optional<ParticipacionEvento> resultado = dao.findByTokenVerificacionWithRelations(p.getTokenVerificacion());

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getId()).isEqualTo(p.getId());
    }

    @Test
    @DisplayName("findByTokenVerificacionWithRelations: token inexistente, vacio")
    void findByTokenVerificacionWithRelations_tokenInexistente_devuelveVacio() {
        assertThat(dao.findByTokenVerificacionWithRelations("no-existe-este-token")).isEmpty();
    }
}
