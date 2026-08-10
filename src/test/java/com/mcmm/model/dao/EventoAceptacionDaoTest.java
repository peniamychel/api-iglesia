package com.mcmm.model.dao;

import com.mcmm.model.entity.Evento;
import com.mcmm.model.entity.EventoAceptacion;
import com.mcmm.model.entity.Iglesia;
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
 * Test de repositorio (nivel "Datos") para EventoAceptacionDao.
 *
 * Ver MiembroIglesiaDaoTest para el porque del perfil "test".
 */
@DataJpaTest
@ActiveProfiles("test")
class EventoAceptacionDaoTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private EventoAceptacionDao dao;

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

    private EventoAceptacion nuevaAceptacion(Evento evento, Long iglesiaId, String estado) {
        EventoAceptacion ea = new EventoAceptacion();
        ea.setEvento(evento);
        ea.setIglesiaId(iglesiaId);
        ea.setEstado(estado);
        return entityManager.persistAndFlush(ea);
    }

    // ───────────────────────── findByEventoIdAndIglesiaId ─────────────────────────

    @Test
    @DisplayName("findByEventoIdAndIglesiaId: encuentra la decision de esa iglesia sobre ese evento")
    void findByEventoIdAndIglesiaId_encuentraLaDecision() {
        Iglesia organizadora = nuevaIglesia("Palmar");
        Evento evento = nuevoEvento("Congreso", organizadora);
        Iglesia invitada = nuevaIglesia("Libertad");
        nuevaAceptacion(evento, invitada.getId(), "ACEPTADO");

        Optional<EventoAceptacion> resultado = dao.findByEventoIdAndIglesiaId(evento.getId(), invitada.getId());

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getEstado()).isEqualTo("ACEPTADO");
    }

    @Test
    @DisplayName("findByEventoIdAndIglesiaId: sin decision registrada, vacio")
    void findByEventoIdAndIglesiaId_sinDecision_devuelveVacio() {
        Iglesia organizadora = nuevaIglesia("Palmar");
        Evento evento = nuevoEvento("Congreso", organizadora);

        assertThat(dao.findByEventoIdAndIglesiaId(evento.getId(), 999L)).isEmpty();
    }

    // ───────────────────────── findByIglesiaId ─────────────────────────

    @Test
    @DisplayName("findByIglesiaId: trae todas las decisiones de esa iglesia, sobre cualquier evento")
    void findByIglesiaId_traeTodasLasDecisiones() {
        Iglesia organizadora = nuevaIglesia("Palmar");
        Iglesia invitada = nuevaIglesia("Libertad");
        Evento e1 = nuevoEvento("Congreso", organizadora);
        Evento e2 = nuevoEvento("Retiro", organizadora);
        nuevaAceptacion(e1, invitada.getId(), "ACEPTADO");
        nuevaAceptacion(e2, invitada.getId(), "ARCHIVADO");

        assertThat(dao.findByIglesiaId(invitada.getId())).hasSize(2);
    }

    // ───────────────────────── deleteByEventoId ─────────────────────────

    @Test
    @DisplayName("deleteByEventoId: borra solo las decisiones del evento indicado")
    void deleteByEventoId_borraSoloLasDelEvento() {
        Iglesia organizadora = nuevaIglesia("Palmar");
        Iglesia invitada = nuevaIglesia("Libertad");
        Evento e1 = nuevoEvento("Congreso", organizadora);
        Evento e2 = nuevoEvento("Retiro", organizadora);
        EventoAceptacion ea1 = nuevaAceptacion(e1, invitada.getId(), "ACEPTADO");
        EventoAceptacion ea2 = nuevaAceptacion(e2, invitada.getId(), "ACEPTADO");

        dao.deleteByEventoId(e1.getId());
        entityManager.flush();
        entityManager.clear();

        assertThat(entityManager.find(EventoAceptacion.class, ea1.getId())).isNull();
        assertThat(entityManager.find(EventoAceptacion.class, ea2.getId())).isNotNull();
    }

    // ───────────────────────── findEventoIdsDecididos ─────────────────────────

    @Test
    @DisplayName("findEventoIdsDecididos: filtra por iglesia y dentro del conjunto de ids dado")
    void findEventoIdsDecididos_filtraPorIglesiaYConjunto() {
        Iglesia organizadora = nuevaIglesia("Palmar");
        Iglesia invitada = nuevaIglesia("Libertad");
        Iglesia otraInvitada = nuevaIglesia("Sacaba");
        Evento decidido = nuevoEvento("Congreso", organizadora);
        Evento noDecidido = nuevoEvento("Retiro", organizadora);
        Evento fueraDelConjunto = nuevoEvento("Vigilia", organizadora);
        nuevaAceptacion(decidido, invitada.getId(), "ACEPTADO");
        nuevaAceptacion(fueraDelConjunto, invitada.getId(), "ACEPTADO");
        nuevaAceptacion(decidido, otraInvitada.getId(), "ACEPTADO"); // otra iglesia, no cuenta

        List<Long> resultado = dao.findEventoIdsDecididos(
                invitada.getId(), List.of(decidido.getId(), noDecidido.getId()));

        assertThat(resultado).containsExactly(decidido.getId());
    }
}
