package com.mcmm.model.dao;

import com.mcmm.model.entity.Cargo;
import com.mcmm.model.entity.Iglesia;
import com.mcmm.model.entity.Miembro;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test de repositorio (nivel "Datos") para CargoDao.
 *
 * Ver MiembroIglesiaDaoTest para el porque del perfil "test".
 */
@DataJpaTest
@ActiveProfiles("test")
class CargoDaoTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CargoDao dao;

    private Iglesia nuevaIglesia(String nombre, boolean estado) {
        Iglesia iglesia = new Iglesia();
        iglesia.setNombre(nombre);
        iglesia.setEstado(estado);
        return entityManager.persistAndFlush(iglesia);
    }

    private Miembro nuevoMiembro(String nombre, String ci, boolean estado) {
        Miembro miembro = new Miembro();
        miembro.setNombre(nombre);
        miembro.setApellido("Apellido");
        miembro.setCi(ci);
        miembro.setEstado(estado);
        return entityManager.persistAndFlush(miembro);
    }

    private Cargo nuevoCargo(Iglesia iglesia, Miembro miembro, boolean estado) {
        Cargo cargo = new Cargo();
        cargo.setIglesia(iglesia);
        cargo.setMiembro(miembro);
        cargo.setEstado(estado);
        return entityManager.persistAndFlush(cargo);
    }

    // ───────────────────────── toggleEstado ─────────────────────────

    @Test
    @DisplayName("toggleEstado: invierte el estado actual")
    void toggleEstado_invierteElEstado() {
        Iglesia iglesia = nuevaIglesia("Palmar", true);
        Miembro miembro = nuevoMiembro("Carlos", "111", true);
        Cargo cargo = nuevoCargo(iglesia, miembro, true);

        dao.toggleEstado(cargo.getId());
        entityManager.clear();

        assertThat(entityManager.find(Cargo.class, cargo.getId()).getEstado()).isFalse();
    }

    // ───────────────────────── findByIglesia_EstadoTrueAndMiembro_EstadoTrue ─────────────────────────

    @Test
    @DisplayName("findByIglesia_EstadoTrueAndMiembro_EstadoTrue: exige ambas relaciones activas")
    void findByIglesiaEstadoTrueAndMiembroEstadoTrue_exigeAmbasActivas() {
        Iglesia iglesiaActiva = nuevaIglesia("Palmar", true);
        Iglesia iglesiaInactiva = nuevaIglesia("Cerrada", false);
        Miembro miembroActivo = nuevoMiembro("Carlos", "201", true);
        Miembro miembroInactivo = nuevoMiembro("Ana", "202", false);

        Cargo valido = nuevoCargo(iglesiaActiva, miembroActivo, true);
        nuevoCargo(iglesiaInactiva, miembroActivo, true); // iglesia cerrada
        nuevoCargo(iglesiaActiva, miembroInactivo, true); // miembro inactivo

        assertThat(dao.findByIglesia_EstadoTrueAndMiembro_EstadoTrue())
                .extracting(Cargo::getId).containsExactly(valido.getId());
    }

    // ───────────────────────── findByIglesiaId ─────────────────────────

    @Test
    @DisplayName("findByIglesiaId: trae todos los cargos de la iglesia, activos e inactivos")
    void findByIglesiaId_traeActivosEInactivos() {
        Iglesia palmar = nuevaIglesia("Palmar", true);
        Iglesia libertad = nuevaIglesia("Libertad", true);
        Miembro miembro = nuevoMiembro("Carlos", "301", true);
        nuevoCargo(palmar, miembro, true);
        nuevoCargo(palmar, nuevoMiembro("Ana", "302", true), false); // inactivo, igual cuenta
        nuevoCargo(libertad, nuevoMiembro("Beto", "303", true), true);

        assertThat(dao.findByIglesiaId(palmar.getId())).hasSize(2);
    }

    // ───────────────────────── existsByMiembroIdAndEstadoTrue ─────────────────────────

    @Test
    @DisplayName("existsByMiembroIdAndEstadoTrue: true solo si el miembro tiene un cargo activo")
    void existsByMiembroIdAndEstadoTrue_detectaCargoActivo() {
        Iglesia iglesia = nuevaIglesia("Palmar", true);
        Miembro conCargoActivo = nuevoMiembro("Carlos", "401", true);
        Miembro conCargoInactivo = nuevoMiembro("Ana", "402", true);
        nuevoCargo(iglesia, conCargoActivo, true);
        nuevoCargo(iglesia, conCargoInactivo, false);

        assertThat(dao.existsByMiembroIdAndEstadoTrue(conCargoActivo.getId())).isTrue();
        assertThat(dao.existsByMiembroIdAndEstadoTrue(conCargoInactivo.getId())).isFalse();
    }
}
