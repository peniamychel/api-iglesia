package com.mcmm.model.dao;

import com.mcmm.model.entity.Certificado;
import com.mcmm.model.entity.Evento;
import com.mcmm.model.entity.Iglesia;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test de repositorio (nivel "Datos") para CertificadoDao.
 *
 * Ver MiembroIglesiaDaoTest para el porque del perfil "test".
 */
@DataJpaTest
@ActiveProfiles("test")
class CertificadoDaoTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CertificadoDao dao;

    private Iglesia nuevaIglesia(String nombre) {
        Iglesia iglesia = new Iglesia();
        iglesia.setNombre(nombre);
        iglesia.setEstado(true);
        return entityManager.persistAndFlush(iglesia);
    }

    private Evento nuevoEvento(String nombre, Iglesia iglesia) {
        Evento evento = new Evento();
        evento.setNombre(nombre);
        evento.setIglesia(iglesia);
        return entityManager.persistAndFlush(evento);
    }

    private Certificado nuevoCertificado(Evento evento) {
        Certificado c = new Certificado();
        c.setEvento(evento);
        return entityManager.persistAndFlush(c);
    }

    // ───────────────────────── toggleEstado ─────────────────────────

    @Test
    @DisplayName("toggleEstado: invierte el estado actual")
    void toggleEstado_invierteElEstado() {
        Iglesia iglesia = nuevaIglesia("Palmar");
        Certificado c = nuevoCertificado(nuevoEvento("Retiro", iglesia));
        assertThat(c.getEstado()).isTrue();

        dao.toggleEstado(c.getId());
        entityManager.clear();

        assertThat(entityManager.find(Certificado.class, c.getId()).getEstado()).isFalse();
    }

    // ───────────────────────── findByEventoIglesiaId ─────────────────────────

    @Test
    @DisplayName("findByEventoIglesiaId: trae solo los certificados de eventos de esa iglesia")
    void findByEventoIglesiaId_filtraPorIglesiaDelEvento() {
        Iglesia palmar = nuevaIglesia("Palmar");
        Iglesia libertad = nuevaIglesia("Libertad");
        nuevoCertificado(nuevoEvento("Retiro", palmar));
        nuevoCertificado(nuevoEvento("Congreso", libertad));

        assertThat(dao.findByEventoIglesiaId(palmar.getId())).hasSize(1);
        assertThat(dao.findByEventoIglesiaId(libertad.getId())).hasSize(1);
    }

    // ───────────────────────── existsByEventoId ─────────────────────────

    @Test
    @DisplayName("existsByEventoId: true solo si el evento ya tiene certificado")
    void existsByEventoId_detectaCertificadoExistente() {
        Iglesia iglesia = nuevaIglesia("Palmar");
        Evento conCertificado = nuevoEvento("Retiro", iglesia);
        Evento sinCertificado = nuevoEvento("Congreso", iglesia);
        nuevoCertificado(conCertificado);

        assertThat(dao.existsByEventoId(conCertificado.getId())).isTrue();
        assertThat(dao.existsByEventoId(sinCertificado.getId())).isFalse();
    }

    // ───────────────────────── findEventoIdsConCertificado ─────────────────────────

    @Test
    @DisplayName("findEventoIdsConCertificado: filtra dentro del conjunto de ids dado, sin duplicados")
    void findEventoIdsConCertificado_filtraDentroDelConjunto() {
        Iglesia iglesia = nuevaIglesia("Palmar");
        Evento e1 = nuevoEvento("Retiro", iglesia);
        Evento e2 = nuevoEvento("Congreso", iglesia); // tiene certificado, pero queda fuera del conjunto consultado
        Evento e3 = nuevoEvento("Vigilia", iglesia);
        nuevoCertificado(e1);
        nuevoCertificado(e2);
        nuevoCertificado(e3);

        List<Long> resultado = dao.findEventoIdsConCertificado(List.of(e1.getId(), e3.getId()));

        assertThat(resultado).containsExactlyInAnyOrder(e1.getId(), e3.getId());
    }

    @Test
    @DisplayName("findEventoIdsConCertificado: evento sin certificado no aparece")
    void findEventoIdsConCertificado_ignoraSinCertificado() {
        Iglesia iglesia = nuevaIglesia("Palmar");
        Evento sinCertificado = nuevoEvento("Retiro", iglesia);

        assertThat(dao.findEventoIdsConCertificado(List.of(sinCertificado.getId()))).isEmpty();
    }
}
