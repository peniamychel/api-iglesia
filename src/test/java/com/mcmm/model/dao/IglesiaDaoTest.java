package com.mcmm.model.dao;

import com.mcmm.model.entity.Cargo;
import com.mcmm.model.entity.Iglesia;
import com.mcmm.model.entity.Miembro;
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
 * Test de repositorio (nivel "Datos") para IglesiaDao.
 *
 * Ver MiembroIglesiaDaoTest para el porque del perfil "test".
 */
@DataJpaTest
@ActiveProfiles("test")
class IglesiaDaoTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private IglesiaDao dao;

    private Iglesia nuevaIglesia(String nombre, boolean estado) {
        Iglesia iglesia = new Iglesia();
        iglesia.setNombre(nombre);
        iglesia.setEstado(estado);
        return entityManager.persistAndFlush(iglesia);
    }

    // ───────────────────────── buscarPorNombreIglesia ─────────────────────────

    @Test
    @DisplayName("buscarPorNombreIglesia: encuentra por nombre exacto")
    void buscarPorNombreIglesia_encuentraPorNombreExacto() {
        nuevaIglesia("Palmar", true);

        assertThat(dao.buscarPorNombreIglesia("Palmar")).isNotNull();
        assertThat(dao.buscarPorNombreIglesia("no existe")).isNull();
    }

    // ───────────────────────── buscarPorNombreIglesiaExceptoId ─────────────────────────

    @Test
    @DisplayName("buscarPorNombreIglesiaExceptoId: encuentra otra iglesia con el mismo nombre, pero no a si misma")
    void buscarPorNombreIglesiaExceptoId_excluyeAlPropioId() {
        Iglesia palmar = nuevaIglesia("Palmar", true);

        // Al excluirse a si misma, no deberia encontrar nada (no hay otra "Palmar").
        assertThat(dao.buscarPorNombreIglesiaExceptoId(palmar.getId(), "Palmar")).isNull();
    }

    @Test
    @DisplayName("buscarPorNombreIglesiaExceptoId: sirve para detectar colision de nombre con OTRA iglesia")
    void buscarPorNombreIglesiaExceptoId_detectaColisionConOtraIglesia() {
        nuevaIglesia("Palmar", true);
        Iglesia otra = nuevaIglesia("Otra", true);

        // Simula validar si "otra" puede renombrarse a "Palmar": debe encontrar la colision.
        assertThat(dao.buscarPorNombreIglesiaExceptoId(otra.getId(), "Palmar")).isNotNull();
    }

    // ───────────────────────── findByNombreAndIdNot ─────────────────────────

    @Test
    @DisplayName("findByNombreAndIdNot: mismo comportamiento que la version @Query, via metodo derivado")
    void findByNombreAndIdNot_excluyeAlPropioId() {
        Iglesia palmar = nuevaIglesia("Palmar", true);

        assertThat(dao.findByNombreAndIdNot("Palmar", palmar.getId())).isNull();
    }

    // ───────────────────────── findAllByOrderByCreatedAtDesc ─────────────────────────

    @Test
    @DisplayName("findAllByOrderByCreatedAtDesc: la mas reciente primero")
    void findAllByOrderByCreatedAtDesc_ordenaDescendente() {
        Iglesia primera = nuevaIglesia("Palmar", true);
        Iglesia segunda = nuevaIglesia("Libertad", true);
        // @PrePersist fija createdAt=now() al persistir, y las dos entidades se crean
        // demasiado rapido para garantizar timestamps distintos (dependiendo de la
        // resolucion del reloj, pueden caer en el mismo milisegundo) — se fuerza un
        // orden inequivoco via UPDATE nativo antes de consultar.
        entityManager.getEntityManager()
                .createNativeQuery("UPDATE iglesia SET created_at = '2026-01-01 00:00:00' WHERE id = " + primera.getId())
                .executeUpdate();
        entityManager.getEntityManager()
                .createNativeQuery("UPDATE iglesia SET created_at = '2026-01-02 00:00:00' WHERE id = " + segunda.getId())
                .executeUpdate();
        entityManager.clear();

        List<Iglesia> resultado = dao.findAllByOrderByCreatedAtDesc();

        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).getId()).isEqualTo(segunda.getId());
        assertThat(resultado.get(1).getId()).isEqualTo(primera.getId());
    }

    // ───────────────────────── findAllOrderByOrdenAscAndCreatedAtDesc ─────────────────────────

    @Test
    @DisplayName("findAllOrderByOrdenAscAndCreatedAtDesc: primero por orden, las sin orden (NULL) van al final")
    void findAllOrderByOrdenAscAndCreatedAtDesc_ordenaConNullsAlFinal() {
        Iglesia sinOrden = nuevaIglesia("Sacaba", true); // orden = null
        Iglesia orden2 = nuevaIglesia("Libertad", true);
        orden2.setOrden(2);
        entityManager.persistAndFlush(orden2);
        Iglesia orden1 = nuevaIglesia("Palmar", true);
        orden1.setOrden(1);
        entityManager.persistAndFlush(orden1);

        List<Iglesia> resultado = dao.findAllOrderByOrdenAscAndCreatedAtDesc();

        assertThat(resultado).extracting(Iglesia::getId)
                .containsExactly(orden1.getId(), orden2.getId(), sinOrden.getId());
    }

    // ───────────────────────── findByEstadoTrue ─────────────────────────

    @Test
    @DisplayName("findByEstadoTrue: excluye iglesias inactivas y trae los cargos con su rol/miembro")
    void findByEstadoTrue_excluyeInactivasYCargaCargos() {
        Iglesia activa = nuevaIglesia("Palmar", true);
        nuevaIglesia("Cerrada", false);

        RolCargo rol = new RolCargo();
        rol.setNombre("Pastor");
        rol.setTipo("CARGO");
        entityManager.persistAndFlush(rol);

        Miembro miembro = new Miembro();
        miembro.setNombre("Carlos");
        miembro.setApellido("Perez");
        miembro.setCi("999");
        miembro.setEstado(true);
        entityManager.persistAndFlush(miembro);

        Cargo cargo = new Cargo();
        cargo.setIglesia(activa);
        cargo.setMiembro(miembro);
        cargo.setRolCargo(rol);
        cargo.setEstado(true);
        entityManager.persistAndFlush(cargo);

        entityManager.clear();

        List<Iglesia> resultado = dao.findByEstadoTrue();

        assertThat(resultado).extracting(Iglesia::getNombre).containsExactly("Palmar");
        assertThat(resultado.get(0).getCargos()).hasSize(1);
        assertThat(resultado.get(0).getCargos().get(0).getMiembro().getNombre()).isEqualTo("Carlos");
    }
}
