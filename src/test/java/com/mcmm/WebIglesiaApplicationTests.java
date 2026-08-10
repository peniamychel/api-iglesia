package com.mcmm;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Prueba de integracion: levanta el contexto completo de Spring, asi que
 * necesita la base de datos en marcha y ademas ejecuta los inicializadores de
 * datos semilla contra ella.
 *
 * Va etiquetada como "integracion" y surefire la excluye por defecto, para que
 * `mvnw test` corra solo las pruebas unitarias (sin base y en segundos).
 * Para ejecutarla a proposito:
 *
 *     mvnw.cmd test -Dgroups=integracion
 */
@Tag("integracion")
@SpringBootTest
class WebIglesiaApplicationTests {

	@Test
	void contextLoads() {
	}

}
