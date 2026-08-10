package com.mcmm.model.dao;

import com.mcmm.model.entity.TipoEvento;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test de repositorio (nivel "Datos") para TipoEventoDao — solo tiene un
 * metodo propio, toggleEstado; el resto lo hereda de JpaRepository.
 *
 * Ver MiembroIglesiaDaoTest para el porque del perfil "test".
 */
@DataJpaTest
@ActiveProfiles("test")
class TipoEventoDaoTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TipoEventoDao dao;

    @Test
    @DisplayName("toggleEstado: invierte el estado actual")
    void toggleEstado_invierteElEstado() {
        TipoEvento tipo = new TipoEvento();
        tipo.setNombre("Retiro");
        entityManager.persistAndFlush(tipo);
        assertThat(tipo.getEstado()).isTrue();

        dao.toggleEstado(tipo.getId());
        entityManager.clear();
        assertThat(entityManager.find(TipoEvento.class, tipo.getId()).getEstado()).isFalse();

        dao.toggleEstado(tipo.getId());
        entityManager.clear();
        assertThat(entityManager.find(TipoEvento.class, tipo.getId()).getEstado()).isTrue();
    }
}
