package com.mcmm.model.dao;

import com.mcmm.model.entity.Cargo;
import com.mcmm.model.entity.Evento;
import com.mcmm.model.entity.Iglesia;
import com.mcmm.model.entity.Miembro;
import com.mcmm.model.entity.ResponsableEvento;
import com.mcmm.model.entity.RolCargo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test de repositorio (nivel "Datos") para ResponsableEventoDao.
 *
 * Ver MiembroIglesiaDaoTest para el porque del perfil "test".
 */
@DataJpaTest
@ActiveProfiles("test")
class ResponsableEventoDaoTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ResponsableEventoDao dao;

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

    private Miembro nuevoMiembro(String nombre, String ci) {
        Miembro miembro = new Miembro();
        miembro.setNombre(nombre);
        miembro.setApellido("Apellido");
        miembro.setCi(ci);
        miembro.setEstado(true);
        return entityManager.persistAndFlush(miembro);
    }

    private Cargo nuevoCargo(Miembro miembro, String nombreRol) {
        RolCargo rol = new RolCargo();
        rol.setNombre(nombreRol);
        rol.setTipo("CARGO");
        entityManager.persistAndFlush(rol);

        Cargo cargo = new Cargo();
        cargo.setMiembro(miembro);
        cargo.setRolCargo(rol);
        cargo.setEstado(true);
        return entityManager.persistAndFlush(cargo);
    }

    private ResponsableEvento nuevoResponsable(Evento evento, Cargo cargo) {
        ResponsableEvento r = new ResponsableEvento();
        r.setEvento(evento);
        r.setCargo(cargo);
        return entityManager.persistAndFlush(r);
    }

    // ───────────────────────── toggleEstado ─────────────────────────

    @Test
    @DisplayName("toggleEstado: invierte el estado actual")
    void toggleEstado_invierteElEstado() {
        Iglesia iglesia = nuevaIglesia("Palmar");
        Evento evento = nuevoEvento("Retiro", iglesia);
        Cargo cargo = nuevoCargo(nuevoMiembro("Carlos", "111"), "Diacono");
        ResponsableEvento r = nuevoResponsable(evento, cargo);
        assertThat(r.getEstado()).isTrue();

        dao.toggleEstado(r.getId());
        entityManager.clear();

        assertThat(entityManager.find(ResponsableEvento.class, r.getId()).getEstado()).isFalse();
    }

    // ───────────────────────── deleteByEventoId ─────────────────────────

    @Test
    @DisplayName("deleteByEventoId: borra solo los responsables del evento indicado")
    void deleteByEventoId_borraSoloLosDelEvento() {
        Iglesia iglesia = nuevaIglesia("Palmar");
        Evento evento1 = nuevoEvento("Retiro", iglesia);
        Evento evento2 = nuevoEvento("Congreso", iglesia);
        ResponsableEvento r1 = nuevoResponsable(evento1, nuevoCargo(nuevoMiembro("Carlos", "201"), "Diacono"));
        ResponsableEvento r2 = nuevoResponsable(evento2, nuevoCargo(nuevoMiembro("Ana", "202"), "Diacono"));

        dao.deleteByEventoId(evento1.getId());
        entityManager.flush();
        entityManager.clear();

        assertThat(entityManager.find(ResponsableEvento.class, r1.getId())).isNull();
        assertThat(entityManager.find(ResponsableEvento.class, r2.getId())).isNotNull();
    }

    // ───────────────────────── findByEventoIglesiaId ─────────────────────────

    @Test
    @DisplayName("findByEventoIglesiaId: trae los responsables de eventos de esa iglesia")
    void findByEventoIglesiaId_filtraPorIglesiaDelEvento() {
        Iglesia palmar = nuevaIglesia("Palmar");
        Iglesia libertad = nuevaIglesia("Libertad");
        Evento eventoPalmar = nuevoEvento("Retiro", palmar);
        Evento eventoLibertad = nuevoEvento("Congreso", libertad);
        nuevoResponsable(eventoPalmar, nuevoCargo(nuevoMiembro("Carlos", "301"), "Diacono"));
        nuevoResponsable(eventoLibertad, nuevoCargo(nuevoMiembro("Ana", "302"), "Diacono"));

        List<ResponsableEvento> resultado = dao.findByEventoIglesiaId(palmar.getId());

        assertThat(resultado).hasSize(1);
        assertThat(dao.findByEventoIglesiaId(libertad.getId())).hasSize(1);
    }

    // ───────────────────────── findByEventoIdWithRelations ─────────────────────────

    @Test
    @DisplayName("findByEventoIdWithRelations: trae los responsables del evento con cargo/miembro/rol cargados")
    void findByEventoIdWithRelations_traeLasRelaciones() {
        Iglesia iglesia = nuevaIglesia("Palmar");
        Evento evento = nuevoEvento("Retiro", iglesia);
        Evento otroEvento = nuevoEvento("Congreso", iglesia);
        Miembro miembro = nuevoMiembro("Carlos", "401");
        nuevoResponsable(evento, nuevoCargo(miembro, "Diacono"));
        nuevoResponsable(otroEvento, nuevoCargo(nuevoMiembro("Ana", "402"), "Diacono"));

        List<ResponsableEvento> resultado = dao.findByEventoIdWithRelations(evento.getId());

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getCargo().getMiembro().getNombre()).isEqualTo("Carlos");
        assertThat(resultado.get(0).getCargo().getRolCargo().getNombre()).isEqualTo("Diacono");
    }

    @Test
    @DisplayName("findByEventoIdWithRelations: evento sin responsables, lista vacia")
    void findByEventoIdWithRelations_sinResponsables_devuelveVacio() {
        Iglesia iglesia = nuevaIglesia("Palmar");
        Evento evento = nuevoEvento("Retiro", iglesia);

        assertThat(dao.findByEventoIdWithRelations(evento.getId())).isEmpty();
    }
}
