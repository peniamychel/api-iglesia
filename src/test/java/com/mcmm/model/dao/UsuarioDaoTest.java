package com.mcmm.model.dao;

import com.mcmm.model.entity.Cargo;
import com.mcmm.model.entity.Iglesia;
import com.mcmm.model.entity.Miembro;
import com.mcmm.model.entity.RolCargo;
import com.mcmm.model.entity.Usuario;
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
 * Test de repositorio (nivel "Datos") para UsuarioDao. findByUsername
 * encadena cuatro LEFT JOIN FETCH (usuario -> miembro -> cargos -> rolCargo /
 * iglesia); se prueba explicitamente que un usuario sin miembro vinculado (o
 * un miembro sin cargos) igual se resuelva, ya que una cadena tan larga de
 * joins opcionales es donde mas facil se cuela un INNER JOIN por accidente
 * (ver la regresion de MiembroDao.searchMiembros).
 *
 * Ver MiembroIglesiaDaoTest para el porque del perfil "test".
 */
@DataJpaTest
@ActiveProfiles("test")
class UsuarioDaoTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UsuarioDao dao;

    private Usuario nuevoUsuario(String username, String email, Miembro miembro, Boolean esAdmin) {
        Usuario u = new Usuario();
        u.setUsername(username);
        u.setEmail(email);
        u.setPassword("password-hash");
        u.setMiembro(miembro);
        u.setEsAdmin(esAdmin);
        return entityManager.persistAndFlush(u);
    }

    private Miembro nuevoMiembro(String nombre, String ci) {
        Miembro miembro = new Miembro();
        miembro.setNombre(nombre);
        miembro.setApellido("Apellido");
        miembro.setCi(ci);
        miembro.setEstado(true);
        return entityManager.persistAndFlush(miembro);
    }

    // ───────────────────────── findByEsAdminIsNull ─────────────────────────

    @Test
    @DisplayName("findByEsAdminIsNull: detecta usuarios con esAdmin sin inicializar (backfill)")
    void findByEsAdminIsNull_detectaSinInicializar() {
        nuevoUsuario("con.flag", "con.flag@test.com", null, false);
        // @PrePersist siempre completa esAdmin=false si viene null, asi que la unica forma
        // de simular una fila historica (antes de que la columna existiera) es un INSERT
        // nativo que se salte el ciclo de vida de la entidad.
        entityManager.getEntityManager()
                .createNativeQuery("INSERT INTO usuario (username, email, password, estado, es_admin) " +
                        "VALUES ('sin.flag', 'sin.flag@test.com', 'x', true, NULL)")
                .executeUpdate();
        entityManager.flush();
        entityManager.clear();

        List<Usuario> resultado = dao.findByEsAdminIsNull();

        assertThat(resultado).extracting(Usuario::getUsername).containsExactly("sin.flag");
    }

    // ───────────────────────── findByUsername ─────────────────────────

    @Test
    @DisplayName("findByUsername: resuelve un usuario sin miembro vinculado (los LEFT JOIN no lo excluyen)")
    void findByUsername_sinMiembroVinculado() {
        nuevoUsuario("sinmiembro", "sinmiembro@test.com", null, false);

        Optional<Usuario> resultado = dao.findByUsername("sinmiembro");

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getMiembro()).isNull();
    }

    @Test
    @DisplayName("findByUsername: resuelve un usuario con miembro pero sin cargos")
    void findByUsername_conMiembroSinCargos() {
        Miembro miembro = nuevoMiembro("Carlos", "111");
        nuevoUsuario("carlos", "carlos@test.com", miembro, false);
        entityManager.clear(); // fuerza a leer de la BD, no a reusar la instancia recien persistida

        Optional<Usuario> resultado = dao.findByUsername("carlos");

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getMiembro().getNombre()).isEqualTo("Carlos");
        assertThat(resultado.get().getMiembro().getCargos()).isEmpty();
    }

    @Test
    @DisplayName("findByUsername: carga la cadena completa hasta rolCargo e iglesia del cargo")
    void findByUsername_cargaCadenaCompletaDeCargo() {
        Miembro miembro = nuevoMiembro("Ana", "222");
        Iglesia iglesia = new Iglesia();
        iglesia.setNombre("Palmar");
        iglesia.setEstado(true);
        entityManager.persistAndFlush(iglesia);
        RolCargo rol = new RolCargo();
        rol.setNombre("Pastor");
        rol.setTipo("CARGO");
        entityManager.persistAndFlush(rol);
        Cargo cargo = new Cargo();
        cargo.setMiembro(miembro);
        cargo.setIglesia(iglesia);
        cargo.setRolCargo(rol);
        cargo.setEstado(true);
        entityManager.persistAndFlush(cargo);
        nuevoUsuario("ana", "ana@test.com", miembro, false);
        entityManager.clear();

        Optional<Usuario> resultado = dao.findByUsername("ana");

        assertThat(resultado).isPresent();
        Cargo cargoCargado = resultado.get().getMiembro().getCargos().get(0);
        assertThat(cargoCargado.getRolCargo().getNombre()).isEqualTo("Pastor");
        assertThat(cargoCargado.getIglesia().getNombre()).isEqualTo("Palmar");
    }

    @Test
    @DisplayName("findByUsername: usuario inexistente, vacio")
    void findByUsername_usuarioInexistente_devuelveVacio() {
        assertThat(dao.findByUsername("no-existe")).isEmpty();
    }

    // ───────────────────────── existsByUsername / existsByEmail ─────────────────────────

    @Test
    @DisplayName("existsByUsername / existsByEmail: detectan colision exacta")
    void existsByUsernameYEmail_detectanColision() {
        nuevoUsuario("carlos", "carlos@test.com", null, false);

        assertThat(dao.existsByUsername("carlos")).isTrue();
        assertThat(dao.existsByUsername("otro")).isFalse();
        assertThat(dao.existsByEmail("carlos@test.com")).isTrue();
        assertThat(dao.existsByEmail("otro@test.com")).isFalse();
    }

    // ───────────────────────── getName ─────────────────────────

    @Test
    @DisplayName("getName: encuentra por username exacto, sin las relaciones cargadas de mas")
    void getName_encuentraPorUsername() {
        nuevoUsuario("carlos", "carlos@test.com", null, false);

        assertThat(dao.getName("carlos")).isPresent();
        assertThat(dao.getName("no-existe")).isEmpty();
    }

    // ───────────────────────── existsByMiembroIdAndEstadoTrue ─────────────────────────

    @Test
    @DisplayName("existsByMiembroIdAndEstadoTrue: true solo si el usuario de ese miembro esta activo")
    void existsByMiembroIdAndEstadoTrue_filtraPorEstado() {
        Miembro miembroActivo = nuevoMiembro("Carlos", "301");
        Miembro miembroInactivo = nuevoMiembro("Ana", "302");
        Usuario activo = nuevoUsuario("carlos", "carlos2@test.com", miembroActivo, false);
        activo.setEstado(true);
        entityManager.persistAndFlush(activo);
        Usuario inactivo = nuevoUsuario("ana", "ana2@test.com", miembroInactivo, false);
        inactivo.setEstado(false);
        entityManager.persistAndFlush(inactivo);

        assertThat(dao.existsByMiembroIdAndEstadoTrue(miembroActivo.getId())).isTrue();
        assertThat(dao.existsByMiembroIdAndEstadoTrue(miembroInactivo.getId())).isFalse();
    }
}
