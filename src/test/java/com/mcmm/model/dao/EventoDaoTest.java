package com.mcmm.model.dao;

import com.mcmm.model.entity.Evento;
import com.mcmm.model.entity.Iglesia;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test de repositorio (nivel "Datos") para EventoDao — SOLO las consultas
 * portables a H2. EventoDao tiene 5 @Query(nativeQuery = true) que usan
 * FIND_IN_SET (especifico de MariaDB) para resolver "iglesias_invitadas":
 * findEventosParaIglesia, findEventosArchivadosParaIglesia, findNoArchivados,
 * findArchivadosTodos, findIdsEventosHabilitadosParaIglesia. Esas quedan
 * fuera de este test (ver el comentario en pom.xml sobre H2 y FIND_IN_SET).
 *
 * Ver MiembroIglesiaDaoTest para el porque del perfil "test".
 */
@DataJpaTest
@ActiveProfiles("test")
class EventoDaoTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EventoDao dao;

    private Iglesia nuevaIglesia(String nombre) {
        Iglesia iglesia = new Iglesia();
        iglesia.setNombre(nombre);
        iglesia.setEstado(true);
        return entityManager.persistAndFlush(iglesia);
    }

    private Evento nuevoEvento(String nombre, Iglesia iglesia, Date fechaInicio) {
        Evento evento = new Evento();
        evento.setNombre(nombre);
        evento.setIglesia(iglesia);
        evento.setFechaInicio(fechaInicio);
        return entityManager.persistAndFlush(evento);
    }

    // ───────────────────────── toggleEstado ─────────────────────────

    @Test
    @DisplayName("toggleEstado: invierte el estado actual")
    void toggleEstado_invierteElEstado() {
        Iglesia iglesia = nuevaIglesia("Palmar");
        Evento evento = nuevoEvento("Retiro", iglesia, null);
        assertThat(evento.getEstado()).isTrue();

        dao.toggleEstado(evento.getId());
        entityManager.clear();

        assertThat(entityManager.find(Evento.class, evento.getId()).getEstado()).isFalse();
    }

    // ───────────────────────── setArchivado ─────────────────────────

    @Test
    @DisplayName("setArchivado: fija el flag al valor pedido, no solo lo invierte")
    void setArchivado_fijaElValorPedido() {
        Iglesia iglesia = nuevaIglesia("Palmar");
        Evento evento = nuevoEvento("Retiro", iglesia, null);
        assertThat(evento.getArchivado()).isFalse();

        dao.setArchivado(evento.getId(), true);
        entityManager.clear();
        assertThat(entityManager.find(Evento.class, evento.getId()).getArchivado()).isTrue();

        dao.setArchivado(evento.getId(), true); // idempotente
        entityManager.clear();
        assertThat(entityManager.find(Evento.class, evento.getId()).getArchivado()).isTrue();

        dao.setArchivado(evento.getId(), false);
        entityManager.clear();
        assertThat(entityManager.find(Evento.class, evento.getId()).getArchivado()).isFalse();
    }

    // ───────────────────────── findByIglesiaId ─────────────────────────

    @Test
    @DisplayName("findByIglesiaId: trae los eventos organizados por esa iglesia")
    void findByIglesiaId_filtraPorIglesia() {
        Iglesia palmar = nuevaIglesia("Palmar");
        Iglesia libertad = nuevaIglesia("Libertad");
        nuevoEvento("Retiro", palmar, null);
        nuevoEvento("Congreso", libertad, null);

        assertThat(dao.findByIglesiaId(palmar.getId())).hasSize(1);
    }

    // ───────────────────────── findByFechaInicioBetween ─────────────────────────

    @Test
    @DisplayName("findByFechaInicioBetween: filtra por rango de fecha de inicio, inclusive")
    void findByFechaInicioBetween_filtraPorRangoInclusive() {
        Iglesia iglesia = nuevaIglesia("Palmar");
        Date limiteInferior = new Date(2026 - 1900, 0, 1);
        Date limiteSuperior = new Date(2026 - 1900, 0, 31);
        Date fueraDeRango = new Date(2026 - 1900, 1, 1);
        nuevoEvento("Dentro1", iglesia, limiteInferior);
        nuevoEvento("Dentro2", iglesia, limiteSuperior);
        nuevoEvento("Fuera", iglesia, fueraDeRango);

        List<Evento> resultado = dao.findByFechaInicioBetween(limiteInferior, limiteSuperior);

        assertThat(resultado).extracting(Evento::getNombre).containsExactlyInAnyOrder("Dentro1", "Dentro2");
    }
}
