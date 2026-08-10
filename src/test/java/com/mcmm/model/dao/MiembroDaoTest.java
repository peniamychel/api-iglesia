package com.mcmm.model.dao;

import com.mcmm.model.entity.Cargo;
import com.mcmm.model.entity.Iglesia;
import com.mcmm.model.entity.Miembro;
import com.mcmm.model.entity.MiembroIglesia;
import com.mcmm.model.entity.RolCargo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test de repositorio (nivel "Datos") para MiembroDao. Las dos consultas
 * "sin iglesia" excluyen por subquery NOT IN, y searchMiembros combina cinco
 * filtros opcionales (texto libre en 5 columnas + estado + iglesia) detras
 * de un LEFT JOIN condicional — exactamente el tipo de logica que rompe
 * silenciosamente al tocarla y que un mock del DAO no puede detectar.
 *
 * Ver MiembroIglesiaDaoTest para el porque del perfil "test".
 */
@DataJpaTest
@ActiveProfiles("test")
class MiembroDaoTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MiembroDao dao;

    private Iglesia nuevaIglesia(String nombre) {
        Iglesia iglesia = new Iglesia();
        iglesia.setNombre(nombre);
        iglesia.setEstado(true);
        return entityManager.persistAndFlush(iglesia);
    }

    private Miembro nuevoMiembro(String nombre, String apellido, String ci, boolean estado) {
        Miembro miembro = new Miembro();
        miembro.setNombre(nombre);
        miembro.setApellido(apellido);
        miembro.setCi(ci);
        miembro.setEstado(estado);
        return entityManager.persistAndFlush(miembro);
    }

    private void asignarAIglesia(Miembro miembro, Iglesia iglesia, boolean activa) {
        MiembroIglesia mi = new MiembroIglesia();
        mi.setMiembro(miembro);
        mi.setIglesia(iglesia);
        mi.setEstado(activa);
        entityManager.persistAndFlush(mi);
    }

    private void asignarCargo(Miembro miembro, String nombreRolCargo, boolean activo) {
        RolCargo rol = new RolCargo();
        rol.setNombre(nombreRolCargo);
        rol.setTipo("CARGO");
        entityManager.persistAndFlush(rol);

        Cargo cargo = new Cargo();
        cargo.setMiembro(miembro);
        cargo.setRolCargo(rol);
        cargo.setEstado(activo);
        entityManager.persistAndFlush(cargo);
    }

    // ───────────────────────── findByCi ─────────────────────────

    @Test
    @DisplayName("findByCi: encuentra por CI exacto")
    void findByCi_encuentraPorCiExacto() {
        nuevoMiembro("Carlos", "Perez", "1001", true);

        assertThat(dao.findByCi("1001")).isNotNull();
        assertThat(dao.findByCi("1001").getNombre()).isEqualTo("Carlos");
    }

    @Test
    @DisplayName("findByCi: CI inexistente devuelve null")
    void findByCi_ciInexistente_devuelveNull() {
        assertThat(dao.findByCi("no-existe")).isNull();
    }

    // ───────────────────────── findSinIglesia ─────────────────────────

    @Test
    @DisplayName("findSinIglesia: incluye al miembro activo sin ninguna asignacion")
    void findSinIglesia_incluyeSinAsignacion() {
        Miembro sinAsignacion = nuevoMiembro("Carlos", "Perez", "1101", true);

        assertThat(dao.findSinIglesia()).extracting(Miembro::getId).contains(sinAsignacion.getId());
    }

    @Test
    @DisplayName("findSinIglesia: excluye al miembro con una asignacion activa")
    void findSinIglesia_excluyeConAsignacionActiva() {
        Iglesia palmar = nuevaIglesia("Palmar");
        Miembro asignado = nuevoMiembro("Ana", "Lopez", "1102", true);
        asignarAIglesia(asignado, palmar, true);

        assertThat(dao.findSinIglesia()).extracting(Miembro::getId).doesNotContain(asignado.getId());
    }

    @Test
    @DisplayName("findSinIglesia: una asignacion inactiva no cuenta, sigue apareciendo como sin iglesia")
    void findSinIglesia_incluyeConSoloAsignacionInactiva() {
        Iglesia palmar = nuevaIglesia("Palmar");
        Miembro exMiembro = nuevoMiembro("Beto", "Ruiz", "1103", true);
        asignarAIglesia(exMiembro, palmar, false); // traspaso viejo, ya no activo

        assertThat(dao.findSinIglesia()).extracting(Miembro::getId).contains(exMiembro.getId());
    }

    @Test
    @DisplayName("findSinIglesia: un miembro inactivo no aparece aunque no tenga iglesia")
    void findSinIglesia_excluyeMiembroInactivo() {
        Miembro inactivo = nuevoMiembro("Dario", "Soto", "1104", false);

        assertThat(dao.findSinIglesia()).extracting(Miembro::getId).doesNotContain(inactivo.getId());
    }

    // ───────────────────────── findSinIglesiaParaAsignacion ─────────────────────────

    @Test
    @DisplayName("findSinIglesiaParaAsignacion: excluye a quien tiene un cargo activo que matchea el patron de pastor")
    void findSinIglesiaParaAsignacion_excluyePastorActivo() {
        Miembro pastor = nuevoMiembro("Carlos", "Perez", "1201", true);
        asignarCargo(pastor, "Pastor Principal", true);
        Miembro sinCargo = nuevoMiembro("Ana", "Lopez", "1202", true);

        List<Miembro> resultado = dao.findSinIglesiaParaAsignacion("%PASTOR%");

        assertThat(resultado).extracting(Miembro::getId).doesNotContain(pastor.getId());
        assertThat(resultado).extracting(Miembro::getId).contains(sinCargo.getId());
    }

    @Test
    @DisplayName("findSinIglesiaParaAsignacion: un cargo activo que no matchea el patron no excluye al miembro")
    void findSinIglesiaParaAsignacion_incluyeConCargoNoPastor() {
        Miembro lider = nuevoMiembro("Beto", "Ruiz", "1203", true);
        asignarCargo(lider, "Lider de Jovenes", true);

        assertThat(dao.findSinIglesiaParaAsignacion("%PASTOR%"))
                .extracting(Miembro::getId).contains(lider.getId());
    }

    @Test
    @DisplayName("findSinIglesiaParaAsignacion: un cargo de pastor INACTIVO no excluye al miembro")
    void findSinIglesiaParaAsignacion_incluyeConCargoPastorInactivo() {
        Miembro exPastor = nuevoMiembro("Dario", "Soto", "1204", true);
        asignarCargo(exPastor, "Pastor Principal", false); // ya deslindado

        assertThat(dao.findSinIglesiaParaAsignacion("%PASTOR%"))
                .extracting(Miembro::getId).contains(exPastor.getId());
    }

    @Test
    @DisplayName("findSinIglesiaParaAsignacion: sigue excluyendo a quien tiene asignacion de iglesia activa")
    void findSinIglesiaParaAsignacion_excluyeConIglesiaActiva() {
        Iglesia palmar = nuevaIglesia("Palmar");
        Miembro asignado = nuevoMiembro("Elena", "Vega", "1205", true);
        asignarAIglesia(asignado, palmar, true);

        assertThat(dao.findSinIglesiaParaAsignacion("%PASTOR%"))
                .extracting(Miembro::getId).doesNotContain(asignado.getId());
    }

    // ───────────────────────── searchMiembros ─────────────────────────

    @Test
    @DisplayName("searchMiembros: sin filtros, pagina sobre todos")
    void searchMiembros_sinFiltros_traeTodos() {
        nuevoMiembro("Carlos", "Perez", "1301", true);
        nuevoMiembro("Ana", "Lopez", "1302", true);

        Page<Miembro> resultado = dao.searchMiembros(null, null, null, PageRequest.of(0, 10));

        assertThat(resultado.getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("searchMiembros: el texto libre matchea nombre, apellido o CI sin importar mayusculas")
    void searchMiembros_textoLibre_matcheaVariasColumnas() {
        nuevoMiembro("Carlos", "Perez", "1303", true);
        nuevoMiembro("Ana", "Carlos", "1304", true); // matchea por apellido
        nuevoMiembro("Beto", "Ruiz", "1305carlos", true); // matchea por ci
        nuevoMiembro("Dario", "Soto", "1306", true); // no matchea

        Page<Miembro> resultado = dao.searchMiembros("carlos", null, null, PageRequest.of(0, 10));

        assertThat(resultado.getTotalElements()).isEqualTo(3);
    }

    @Test
    @DisplayName("searchMiembros: filtra por estado cuando se especifica")
    void searchMiembros_filtraPorEstado() {
        nuevoMiembro("Carlos", "Perez", "1307", true);
        nuevoMiembro("Ana", "Lopez", "1308", false);

        Page<Miembro> resultado = dao.searchMiembros(null, false, null, PageRequest.of(0, 10));

        assertThat(resultado.getTotalElements()).isEqualTo(1);
        assertThat(resultado.getContent().get(0).getCi()).isEqualTo("1308");
    }

    @Test
    @DisplayName("searchMiembros: iglesiaNombre = 'all' no filtra, incluye tambien a los miembros sin iglesia asignada")
    void searchMiembros_iglesiaAll_noFiltra() {
        // Regresion: mi.iglesia.nombre referenciado directamente en el WHERE hacia que
        // Hibernate agregara un INNER JOIN implicito a iglesia encadenado despues del LEFT
        // JOIN a miembros_iglesia. Un miembro sin asignacion activa (mi = NULL) quedaba
        // eliminado de TODO el resultado por ese INNER JOIN, sin importar que 'all' deberia
        // traer todo — los JOIN se resuelven antes que el WHERE. "all" es ademas el valor
        // que el frontend manda siempre por defecto (miembro.service.ts), asi que el bug
        // afectaba la carga inicial de la lista de Miembros en cada visita, no un caso raro.
        Iglesia palmar = nuevaIglesia("Palmar");
        Miembro asignado = nuevoMiembro("Carlos", "Perez", "1309", true);
        asignarAIglesia(asignado, palmar, true);
        nuevoMiembro("Ana", "Lopez", "1310", true); // sin iglesia

        Page<Miembro> resultado = dao.searchMiembros(null, null, "all", PageRequest.of(0, 10));

        assertThat(resultado.getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("searchMiembros: iglesiaNombre especifico solo trae a los asignados activamente a esa iglesia")
    void searchMiembros_iglesiaEspecifica_filtraPorAsignacionActiva() {
        Iglesia palmar = nuevaIglesia("Palmar");
        Iglesia libertad = nuevaIglesia("Libertad");
        Miembro enPalmar = nuevoMiembro("Carlos", "Perez", "1311", true);
        asignarAIglesia(enPalmar, palmar, true);
        Miembro exPalmar = nuevoMiembro("Ana", "Lopez", "1312", true);
        asignarAIglesia(exPalmar, palmar, false); // ya no esta activo alli
        Miembro enLibertad = nuevoMiembro("Beto", "Ruiz", "1313", true);
        asignarAIglesia(enLibertad, libertad, true);

        Page<Miembro> resultado = dao.searchMiembros(null, null, "Palmar", PageRequest.of(0, 10));

        assertThat(resultado.getContent()).extracting(Miembro::getId).containsExactly(enPalmar.getId());
    }

    @Test
    @DisplayName("searchMiembros: combina texto libre y estado a la vez")
    void searchMiembros_combinaTextoYEstado() {
        nuevoMiembro("Carlos", "Perez", "1314", true);
        nuevoMiembro("Carla", "Gomez", "1315", false);

        Page<Miembro> resultado = dao.searchMiembros("car", true, null, PageRequest.of(0, 10));

        assertThat(resultado.getContent()).extracting(Miembro::getCi).containsExactly("1314");
    }

    @Test
    @DisplayName("searchMiembros: respeta el tamaño de pagina pedido")
    void searchMiembros_respetaPaginado() {
        for (int i = 0; i < 5; i++) {
            nuevoMiembro("Miembro" + i, "Apellido", "20" + i, true);
        }

        Page<Miembro> primeraPagina = dao.searchMiembros(null, null, null, PageRequest.of(0, 2));

        assertThat(primeraPagina.getContent()).hasSize(2);
        assertThat(primeraPagina.getTotalElements()).isEqualTo(5);
        assertThat(primeraPagina.getTotalPages()).isEqualTo(3);
    }
}
