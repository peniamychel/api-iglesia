package com.mcmm.model.dao;

import com.mcmm.model.entity.Iglesia;
import com.mcmm.model.entity.Ofrenda;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * Test de repositorio (nivel "Datos") para OfrendaDao. Todas sus consultas
 * comparten el filtro "(estado IS NULL OR estado = true)" para excluir bajas
 * logicas de forma NULL-safe (filas viejas sin la columna quedan estado=NULL
 * y deben seguir contando como activas) — es justo el tipo de detalle que un
 * mock no verifica: si alguien lo reemplaza por "estado = true" a secas, las
 * ofrendas historicas desaparecerian silenciosamente de los informes.
 * Tambien cubre las sumas SUM/COALESCE (deben dar 0.0, no null, sin filas).
 *
 * Ver MiembroIglesiaDaoTest para el porque del perfil "test".
 */
@DataJpaTest
@ActiveProfiles("test")
class OfrendaDaoTest {

    private static final String INGRESO = "INGRESO";
    private static final String EGRESO = "EGRESO";

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private OfrendaDao dao;

    private Iglesia nuevaIglesia(String nombre) {
        Iglesia iglesia = new Iglesia();
        iglesia.setNombre(nombre);
        iglesia.setEstado(true);
        return entityManager.persistAndFlush(iglesia);
    }

    private Ofrenda nuevaOfrenda(Iglesia iglesia, String tipo, double monto, LocalDate fecha, Boolean estado) {
        Ofrenda o = new Ofrenda();
        o.setIglesia(iglesia);
        o.setTipoMovimiento(tipo);
        o.setMonto(monto);
        o.setFechaRecaudacion(Date.valueOf(fecha));
        o.setEstado(estado);
        return entityManager.persistAndFlush(o);
    }

    // ───────────────────────── findAllActive ─────────────────────────

    @Test
    @DisplayName("findAllActive: excluye estado=false, incluye estado=true y estado=NULL")
    void findAllActive_excluyeSoloLasInactivas() {
        Iglesia iglesia = nuevaIglesia("Palmar");
        nuevaOfrenda(iglesia, INGRESO, 100.0, LocalDate.of(2026, 1, 10), true);
        nuevaOfrenda(iglesia, INGRESO, 200.0, LocalDate.of(2026, 1, 11), false); // borrada
        nuevaOfrenda(iglesia, INGRESO, 300.0, LocalDate.of(2026, 1, 12), null); // fila historica sin columna

        List<Ofrenda> resultado = dao.findAllActive();

        assertThat(resultado).hasSize(2);
        assertThat(resultado).extracting(Ofrenda::getMonto).containsExactlyInAnyOrder(100.0, 300.0);
    }

    // ───────────────────────── findByIglesiaId ─────────────────────────

    @Test
    @DisplayName("findByIglesiaId: filtra por iglesia y excluye inactivas")
    void findByIglesiaId_filtraPorIglesiaYExcluyeInactivas() {
        Iglesia palmar = nuevaIglesia("Palmar");
        Iglesia libertad = nuevaIglesia("Libertad");
        nuevaOfrenda(palmar, INGRESO, 100.0, LocalDate.of(2026, 1, 10), true);
        nuevaOfrenda(palmar, INGRESO, 200.0, LocalDate.of(2026, 1, 11), false);
        nuevaOfrenda(libertad, INGRESO, 300.0, LocalDate.of(2026, 1, 12), true);

        List<Ofrenda> resultado = dao.findByIglesiaId(palmar.getId());

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).getMonto()).isEqualTo(100.0);
    }

    // ───────────────────────── findByIglesiaIdAndFechaRecaudacionBetween ─────────────────────────

    @Test
    @DisplayName("findByIglesiaIdAndFechaRecaudacionBetween: filtra por iglesia, rango de fecha (inclusive) y estado")
    void findByIglesiaIdAndFechaRecaudacionBetween_filtraPorRangoInclusive() {
        Iglesia palmar = nuevaIglesia("Palmar");
        Iglesia libertad = nuevaIglesia("Libertad");
        nuevaOfrenda(palmar, INGRESO, 100.0, LocalDate.of(2026, 1, 1), true); // limite inferior
        nuevaOfrenda(palmar, INGRESO, 200.0, LocalDate.of(2026, 1, 31), true); // limite superior
        nuevaOfrenda(palmar, INGRESO, 999.0, LocalDate.of(2026, 2, 1), true); // fuera de rango
        nuevaOfrenda(palmar, INGRESO, 999.0, LocalDate.of(2026, 1, 15), false); // inactiva
        nuevaOfrenda(libertad, INGRESO, 999.0, LocalDate.of(2026, 1, 15), true); // otra iglesia

        List<Ofrenda> resultado = dao.findByIglesiaIdAndFechaRecaudacionBetween(
                palmar.getId(), Date.valueOf(LocalDate.of(2026, 1, 1)), Date.valueOf(LocalDate.of(2026, 1, 31)));

        assertThat(resultado).hasSize(2);
        assertThat(resultado).extracting(Ofrenda::getMonto).containsExactlyInAnyOrder(100.0, 200.0);
    }

    // ───────────────────────── findByFechaRecaudacionBetween ─────────────────────────

    @Test
    @DisplayName("findByFechaRecaudacionBetween: filtra solo por fecha, sin importar la iglesia")
    void findByFechaRecaudacionBetween_ignoraLaIglesia() {
        Iglesia palmar = nuevaIglesia("Palmar");
        Iglesia libertad = nuevaIglesia("Libertad");
        nuevaOfrenda(palmar, INGRESO, 100.0, LocalDate.of(2026, 1, 10), true);
        nuevaOfrenda(libertad, INGRESO, 200.0, LocalDate.of(2026, 1, 20), true);
        nuevaOfrenda(libertad, INGRESO, 999.0, LocalDate.of(2026, 3, 1), true); // fuera de rango

        List<Ofrenda> resultado = dao.findByFechaRecaudacionBetween(
                Date.valueOf(LocalDate.of(2026, 1, 1)), Date.valueOf(LocalDate.of(2026, 1, 31)));

        assertThat(resultado).hasSize(2);
    }

    // ───────────────────────── sumMontoByIglesiaAndTipoAndPeriod ─────────────────────────

    @Test
    @DisplayName("sumMontoByIglesiaAndTipoAndPeriod: suma solo el tipo, la iglesia y el periodo pedidos")
    void sumMontoByIglesiaAndTipoAndPeriod_sumaSoloLoQueCorresponde() {
        Iglesia palmar = nuevaIglesia("Palmar");
        Iglesia libertad = nuevaIglesia("Libertad");
        nuevaOfrenda(palmar, INGRESO, 100.0, LocalDate.of(2026, 1, 10), true);
        nuevaOfrenda(palmar, INGRESO, 50.0, LocalDate.of(2026, 1, 20), true);
        nuevaOfrenda(palmar, EGRESO, 30.0, LocalDate.of(2026, 1, 15), true); // otro tipo
        nuevaOfrenda(palmar, INGRESO, 999.0, LocalDate.of(2026, 2, 1), true); // fuera de periodo
        nuevaOfrenda(palmar, INGRESO, 999.0, LocalDate.of(2026, 1, 12), false); // inactiva
        nuevaOfrenda(libertad, INGRESO, 999.0, LocalDate.of(2026, 1, 12), true); // otra iglesia

        Double suma = dao.sumMontoByIglesiaAndTipoAndPeriod(palmar.getId(), INGRESO,
                Date.valueOf(LocalDate.of(2026, 1, 1)), Date.valueOf(LocalDate.of(2026, 1, 31)));

        assertThat(suma).isCloseTo(150.0, within(0.001));
    }

    @Test
    @DisplayName("sumMontoByIglesiaAndTipoAndPeriod: sin ofrendas que califiquen, da 0.0 y no null")
    void sumMontoByIglesiaAndTipoAndPeriod_sinFilas_devuelveCero() {
        Iglesia palmar = nuevaIglesia("Palmar");

        Double suma = dao.sumMontoByIglesiaAndTipoAndPeriod(palmar.getId(), INGRESO,
                Date.valueOf(LocalDate.of(2026, 1, 1)), Date.valueOf(LocalDate.of(2026, 1, 31)));

        assertThat(suma).isEqualTo(0.0);
    }

    // ───────────────────────── sumMontoByTipoAndPeriod ─────────────────────────

    @Test
    @DisplayName("sumMontoByTipoAndPeriod: suma en todas las iglesias, filtrando solo por tipo y periodo")
    void sumMontoByTipoAndPeriod_sumaEnTodasLasIglesias() {
        Iglesia palmar = nuevaIglesia("Palmar");
        Iglesia libertad = nuevaIglesia("Libertad");
        nuevaOfrenda(palmar, INGRESO, 100.0, LocalDate.of(2026, 1, 10), true);
        nuevaOfrenda(libertad, INGRESO, 50.0, LocalDate.of(2026, 1, 20), true);
        nuevaOfrenda(libertad, EGRESO, 30.0, LocalDate.of(2026, 1, 15), true); // otro tipo
        nuevaOfrenda(libertad, INGRESO, 999.0, LocalDate.of(2026, 1, 12), false); // inactiva

        Double suma = dao.sumMontoByTipoAndPeriod(INGRESO,
                Date.valueOf(LocalDate.of(2026, 1, 1)), Date.valueOf(LocalDate.of(2026, 1, 31)));

        assertThat(suma).isCloseTo(150.0, within(0.001));
    }

    @Test
    @DisplayName("sumMontoByTipoAndPeriod: sin ofrendas que califiquen, da 0.0 y no null")
    void sumMontoByTipoAndPeriod_sinFilas_devuelveCero() {
        Double suma = dao.sumMontoByTipoAndPeriod(INGRESO,
                Date.valueOf(LocalDate.of(2026, 1, 1)), Date.valueOf(LocalDate.of(2026, 1, 31)));

        assertThat(suma).isEqualTo(0.0);
    }
}
