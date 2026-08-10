package com.mcmm.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test unitario del limite de intentos de la verificacion publica de
 * certificados. Es logica pura: no necesita Spring, base de datos ni mocks.
 *
 * Lo que protege: el codigo corto impreso tiene 32^4 combinaciones y la pagina
 * de verificacion es publica. Sin este tope, un script podria recorrer todo el
 * espacio y quedarse con los datos de los miembros.
 */
@DisplayName("Limitador de intentos de verificacion")
class LimitadorVerificacionTest {

    private static final int MAX_INTENTOS = 20;

    private LimitadorVerificacion limitador;

    @BeforeEach
    void setUp() {
        limitador = new LimitadorVerificacion();
    }

    @Test
    @DisplayName("Permite hasta 20 intentos seguidos desde la misma IP")
    void permiteHastaElTope() {
        for (int intento = 1; intento <= MAX_INTENTOS; intento++) {
            assertThat(limitador.permitir("10.0.0.1"))
                    .as("intento numero %d", intento)
                    .isTrue();
        }
    }

    @Test
    @DisplayName("Bloquea el intento 21 dentro de la misma ventana")
    void bloqueaAlSuperarElTope() {
        for (int intento = 0; intento < MAX_INTENTOS; intento++) {
            limitador.permitir("10.0.0.1");
        }

        assertThat(limitador.permitir("10.0.0.1")).isFalse();
    }

    @Test
    @DisplayName("El tope es por IP: una agotada no afecta a las demas")
    void elTopeEsPorIp() {
        for (int intento = 0; intento < MAX_INTENTOS; intento++) {
            limitador.permitir("10.0.0.1");
        }

        assertThat(limitador.permitir("10.0.0.1")).isFalse();
        assertThat(limitador.permitir("10.0.0.2")).isTrue();
    }

    @Test
    @DisplayName("Una IP nula o vacia se cuenta como una sola, no queda sin tope")
    void ipDesconocidaTambienSeLimita() {
        for (int intento = 0; intento < MAX_INTENTOS; intento++) {
            limitador.permitir(null);
        }

        assertThat(limitador.permitir(null)).isFalse();
        // Nulo y cadena vacia caen en el mismo cubo: es la misma IP desconocida.
        assertThat(limitador.permitir("   ")).isFalse();
    }

    @Test
    @DisplayName("Al quedar sin cupo informa cuantos segundos falta esperar")
    void informaLaEsperaRestante() {
        for (int intento = 0; intento < MAX_INTENTOS; intento++) {
            limitador.permitir("10.0.0.3");
        }

        long espera = limitador.segundosDeEspera("10.0.0.3");

        assertThat(espera).isBetween(1L, 60L);
    }

    @Test
    @DisplayName("Una IP que nunca consulto arranca con el cupo completo")
    void ipNuevaEmpiezaLibre() {
        assertThat(limitador.permitir("10.0.0.9")).isTrue();
    }
}
