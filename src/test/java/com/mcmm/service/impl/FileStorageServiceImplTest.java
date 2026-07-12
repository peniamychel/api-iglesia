package com.mcmm.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Test unitario puro (sin contexto Spring ni base de datos) que valida el
 * saneamiento contra path traversal de {@link FileStorageServiceImpl}. Cierra
 * la brecha de "verificado solo por revision de codigo" del fix de seguridad.
 */
class FileStorageServiceImplTest {

    @TempDir
    Path tempDir;

    private FileStorageServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new FileStorageServiceImpl(tempDir.toString());
    }

    @Test
    void getFilePath_rutaRelativaNormal_quedaDentroDelDirectorioRaiz() {
        Path resolved = service.getFilePath("usuarios/foto.jpg");
        // startsWith lexico de java.nio (sin toRealPath), igual que hace resolveSafely.
        assertThat(resolved.startsWith(tempDir.toAbsolutePath().normalize())).isTrue();
        assertThat(resolved.getFileName().toString()).isEqualTo("foto.jpg");
    }

    @Test
    void getFilePath_normalizacionQueSigueDentroDelRaiz_esPermitida() {
        // "usuarios/../personas/x.jpg" se normaliza a "personas/x.jpg", aun dentro del raiz.
        assertThatCode(() -> service.getFilePath("usuarios/../personas/x.jpg"))
                .doesNotThrowAnyException();
    }

    @Test
    void getFilePath_traversalConDoblePunto_esRechazado() {
        assertThatThrownBy(() -> service.getFilePath("../../../application.properties"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getFilePath_traversalQueEscapaDesdeSubdirectorio_esRechazado() {
        assertThatThrownBy(() -> service.getFilePath("usuarios/../../secret.txt"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deleteFile_traversal_esRechazadoAntesDeTocarElDisco() {
        assertThatThrownBy(() -> service.deleteFile("../../application.properties"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getFilePath_rutaNula_esRechazada() {
        assertThatThrownBy(() -> service.getFilePath(null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
