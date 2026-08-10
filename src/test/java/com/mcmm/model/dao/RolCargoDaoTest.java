package com.mcmm.model.dao;

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
 * Test de repositorio (nivel "Datos") para RolCargoDao.
 *
 * Ver MiembroIglesiaDaoTest para el porque del perfil "test".
 */
@DataJpaTest
@ActiveProfiles("test")
class RolCargoDaoTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RolCargoDao dao;

    private RolCargo nuevoRolCargo(String nombre, String nombreRol, boolean estado) {
        RolCargo rol = new RolCargo();
        rol.setNombre(nombre);
        rol.setNombreRol(nombreRol);
        rol.setTipo("CARGO");
        rol.setEstado(estado);
        return entityManager.persistAndFlush(rol);
    }

    // ───────────────────────── findByNombre / findByNombreRol ─────────────────────────

    @Test
    @DisplayName("findByNombre: encuentra por nombre exacto")
    void findByNombre_encuentraPorNombreExacto() {
        nuevoRolCargo("Pastor Principal", "PASTOR", true);

        assertThat(dao.findByNombre("Pastor Principal")).isPresent();
        assertThat(dao.findByNombre("no existe")).isEmpty();
    }

    @Test
    @DisplayName("findByNombreRol: encuentra por el codigo de rol, no por el nombre visible")
    void findByNombreRol_encuentraPorCodigoDeRol() {
        nuevoRolCargo("Pastor Principal", "PASTOR", true);

        assertThat(dao.findByNombreRol("PASTOR")).isPresent();
        assertThat(dao.findByNombreRol("Pastor Principal")).isEmpty();
    }

    // ───────────────────────── findByEstadoTrueAndNombreRolNot ─────────────────────────

    @Test
    @DisplayName("findByEstadoTrueAndNombreRolNot: excluye inactivos y el codigo de rol indicado")
    void findByEstadoTrueAndNombreRolNot_excluyeInactivosYElCodigoDado() {
        nuevoRolCargo("Pastor Principal", "PASTOR", true);
        nuevoRolCargo("Diacono", "DIACONO", true);
        nuevoRolCargo("Lider Cerrado", "LIDER", false); // inactivo

        List<RolCargo> resultado = dao.findByEstadoTrueAndNombreRolNot("PASTOR");

        assertThat(resultado).extracting(RolCargo::getNombreRol).containsExactly("DIACONO");
    }

    // ───────────────────────── toggleEstado ─────────────────────────

    @Test
    @DisplayName("toggleEstado: invierte el estado actual")
    void toggleEstado_invierteElEstado() {
        RolCargo rol = nuevoRolCargo("Diacono", "DIACONO", true);

        dao.toggleEstado(rol.getId());
        entityManager.clear();

        assertThat(entityManager.find(RolCargo.class, rol.getId()).getEstado()).isFalse();
    }
}
