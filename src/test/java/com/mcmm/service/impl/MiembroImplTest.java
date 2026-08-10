package com.mcmm.service.impl;

import com.mcmm.exception.NotFoundExceptionResource;
import com.mcmm.model.dao.IglesiaDao;
import com.mcmm.model.dao.MiembroDao;
import com.mcmm.model.dao.MiembroIglesiaDao;
import com.mcmm.model.dto.MiembroDto.MiembroDto;
import com.mcmm.model.dto.MiembroDto.MiembroImportResultDto;
import com.mcmm.model.entity.Iglesia;
import com.mcmm.model.entity.Miembro;
import com.mcmm.service.FileStorageService;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.modelmapper.ModelMapper;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Cubre sobre todo la importacion masiva por Excel (parseFila y sus validaciones:
 * CI faltante/duplicado, nombre/apellido faltante, sexo y fecha tolerantes a
 * formato) por ser la logica menos trivial de la clase, y el manejo de errores
 * de delete()/update() que difiere a proposito de CertificadoImpl (ver el
 * comentario de delete() en produccion: un fallo al borrar la foto de un
 * miembro NO aborta el borrado del registro, a diferencia de un certificado).
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MiembroImplTest {

    @Mock private ModelMapper modelMapper;
    @Mock private MiembroDao miembroDao;
    @Mock private FileStorageService fileStorageService;
    @Mock private IglesiaDao iglesiaDao;
    @Mock private MiembroIglesiaDao miembroIglesiaDao;

    private MiembroImpl service;

    @BeforeEach
    void setUp() {
        service = new MiembroImpl(modelMapper, miembroDao, fileStorageService, iglesiaDao, miembroIglesiaDao);
        ReflectionTestUtils.setField(service, "uploadDir", "/uploads");
        when(modelMapper.map(any(Miembro.class), eq(MiembroDto.class)))
                .thenAnswer(inv -> MiembroDto.builder().build());
    }

    private MultipartFile excelDe(String... filasSeparadasPorPuntoYComa) throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Miembros");
            sheet.createRow(0); // header, se ignora
            int r = 1;
            for (String fila : filasSeparadasPorPuntoYComa) {
                Row row = sheet.createRow(r++);
                String[] celdas = fila.split(";", -1);
                for (int c = 0; c < celdas.length; c++) {
                    row.createCell(c).setCellValue(celdas[c]);
                }
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return new MockMultipartFile("file", "miembros.xlsx",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", out.toByteArray());
        }
    }

    // Columnas 0-10: ci;nombre;apellido;fechaNac;celular;sexo;direccion;fechaConv;lugarConv;interventores;detalles
    private static final String FILA_VALIDA = "111;Carlos;Perez;;;M;;;;;";

    // ───────────────────────── importFromExcel ─────────────────────────

    @Test
    @DisplayName("importFromExcel: fila valida se registra y se vincula a la iglesia dada")
    void importFromExcel_filaValida_seRegistraYVincula() throws IOException {
        Iglesia iglesia = new Iglesia();
        iglesia.setId(1L);
        iglesia.setNombre("Palmar");
        when(iglesiaDao.findById(1L)).thenReturn(Optional.of(iglesia));
        when(miembroDao.findByCi("111")).thenReturn(null);
        when(miembroDao.save(any(Miembro.class))).thenAnswer(inv -> inv.getArgument(0));

        MiembroImportResultDto resultado = service.importFromExcel(excelDe(FILA_VALIDA), 1L);

        assertThat(resultado.getImported()).isEqualTo(1);
        assertThat(resultado.getOmitidos()).isZero();
        verify(miembroIglesiaDao).save(any());
    }

    @Test
    @DisplayName("importFromExcel: CI vacio se rechaza con motivo, sin registrar nada")
    void importFromExcel_ciVacio_seRechaza() throws IOException {
        Iglesia iglesia = new Iglesia();
        iglesia.setId(1L);
        iglesia.setNombre("Palmar");
        when(iglesiaDao.findById(1L)).thenReturn(Optional.of(iglesia));

        MiembroImportResultDto resultado = service.importFromExcel(excelDe(";Carlos;Perez;;;M;;;;;"), 1L);

        assertThat(resultado.getOmitidos()).isEqualTo(1);
        assertThat(resultado.getErrores().get(0).getMotivo()).isEqualTo("Falta el CI");
        verify(miembroDao, never()).save(any());
    }

    @Test
    @DisplayName("importFromExcel: CI ya registrado se rechaza, no crea un duplicado")
    void importFromExcel_ciDuplicado_seRechaza() throws IOException {
        Iglesia iglesia = new Iglesia();
        iglesia.setId(1L);
        iglesia.setNombre("Palmar");
        when(iglesiaDao.findById(1L)).thenReturn(Optional.of(iglesia));
        when(miembroDao.findByCi("111")).thenReturn(new Miembro());

        MiembroImportResultDto resultado = service.importFromExcel(excelDe(FILA_VALIDA), 1L);

        assertThat(resultado.getOmitidos()).isEqualTo(1);
        assertThat(resultado.getErrores().get(0).getMotivo()).isEqualTo("CI ya registrado");
        verify(miembroDao, never()).save(any());
    }

    @Test
    @DisplayName("importFromExcel: falta nombre o apellido se rechaza")
    void importFromExcel_faltaNombreOApellido_seRechaza() throws IOException {
        Iglesia iglesia = new Iglesia();
        iglesia.setId(1L);
        iglesia.setNombre("Palmar");
        when(iglesiaDao.findById(1L)).thenReturn(Optional.of(iglesia));
        when(miembroDao.findByCi("111")).thenReturn(null);

        MiembroImportResultDto resultado = service.importFromExcel(excelDe("111;;Perez;;;M;;;;;"), 1L);

        assertThat(resultado.getErrores().get(0).getMotivo()).isEqualTo("Falta nombre o apellido");
    }

    @Test
    @DisplayName("importFromExcel: fila totalmente vacia se ignora, ni se cuenta como error")
    void importFromExcel_filaVacia_seIgnora() throws IOException {
        Iglesia iglesia = new Iglesia();
        iglesia.setId(1L);
        iglesia.setNombre("Palmar");
        when(iglesiaDao.findById(1L)).thenReturn(Optional.of(iglesia));

        MiembroImportResultDto resultado = service.importFromExcel(excelDe(";;;;;;;;;;"), 1L);

        assertThat(resultado.getImported()).isZero();
        assertThat(resultado.getOmitidos()).isZero();
    }

    @Test
    @DisplayName("importFromExcel: normaliza sexo (M/F/palabras completas) al estandar Hombre/Mujer")
    void importFromExcel_normalizaSexo() throws IOException {
        Iglesia iglesia = new Iglesia();
        iglesia.setId(1L);
        iglesia.setNombre("Palmar");
        when(iglesiaDao.findById(1L)).thenReturn(Optional.of(iglesia));
        ArgumentCaptor<Miembro> captor = ArgumentCaptor.forClass(Miembro.class);
        when(miembroDao.save(captor.capture())).thenAnswer(inv -> inv.getArgument(0));

        service.importFromExcel(excelDe(
                "111;Carlos;Perez;;;varon;;;;;",
                "222;Ana;Lopez;;;femenino;;;;;",
                "333;Beto;Ruiz;;;X;;;;;" // no reconocido: se conserva tal cual
        ), 1L);

        List<Miembro> guardados = captor.getAllValues();
        assertThat(guardados).extracting(Miembro::getSexo).containsExactly("Hombre", "Mujer", "X");
    }

    @Test
    @DisplayName("importFromExcel: iglesia inexistente lanza NotFoundExceptionResource")
    void importFromExcel_iglesiaInexistente_lanzaNotFound() throws IOException {
        when(iglesiaDao.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.importFromExcel(excelDe(FILA_VALIDA), 99L))
                .isInstanceOf(NotFoundExceptionResource.class);
    }

    // ───────────────────────── importFromExcelPorNombreIglesia ─────────────────────────

    @Test
    @DisplayName("importFromExcelPorNombreIglesia: matchea el nombre de iglesia sin importar mayusculas/espacios")
    void importFromExcelPorNombreIglesia_matcheaNombreCaseInsensitive() throws IOException {
        Iglesia palmar = new Iglesia();
        palmar.setId(1L);
        palmar.setNombre("Palmar");
        when(iglesiaDao.findByEstadoTrue()).thenReturn(List.of(palmar));
        when(miembroDao.findByCi("111")).thenReturn(null);
        when(miembroDao.save(any(Miembro.class))).thenAnswer(inv -> inv.getArgument(0));

        // Columna 11 (indice) = nombre de iglesia, con espacios y mayusculas distintas.
        MiembroImportResultDto resultado = service.importFromExcelPorNombreIglesia(
                excelDe("111;Carlos;Perez;;;M;;;;;; PALMAR "));

        assertThat(resultado.getImported()).isEqualTo(1);
    }

    @Test
    @DisplayName("importFromExcelPorNombreIglesia: nombre de iglesia que no matchea ninguna activa, se rechaza")
    void importFromExcelPorNombreIglesia_iglesiaInvalida_seRechaza() throws IOException {
        when(iglesiaDao.findByEstadoTrue()).thenReturn(List.of());
        when(miembroDao.findByCi("111")).thenReturn(null);

        MiembroImportResultDto resultado = service.importFromExcelPorNombreIglesia(
                excelDe("111;Carlos;Perez;;;M;;;;;;Inexistente"));

        assertThat(resultado.getOmitidos()).isEqualTo(1);
        assertThat(resultado.getErrores().get(0).getMotivo()).isEqualTo("Iglesia inválida o faltante");
        verify(miembroDao, never()).save(any());
    }

    @Test
    @DisplayName("importFromExcelPorNombreIglesia: datos de miembro incompletos pero con iglesia, se rechaza con motivo propio")
    void importFromExcelPorNombreIglesia_datosIncompletos_seRechaza() throws IOException {
        Iglesia palmar = new Iglesia();
        palmar.setId(1L);
        palmar.setNombre("Palmar");
        when(iglesiaDao.findByEstadoTrue()).thenReturn(List.of(palmar));

        MiembroImportResultDto resultado = service.importFromExcelPorNombreIglesia(
                excelDe(";;;;;;;;;;;Palmar"));

        assertThat(resultado.getErrores().get(0).getMotivo()).isEqualTo("Faltan datos del miembro");
    }

    // ───────────────────────── delete ─────────────────────────

    @Test
    @DisplayName("delete: si falla borrar la foto, NO propaga el error y borra igual el registro")
    void delete_fotoFallaAlBorrar_noPropagaYBorraElRegistro() throws IOException {
        Miembro miembro = new Miembro();
        miembro.setId(1L);
        miembro.setUriFoto("foto.jpg");
        when(miembroDao.findById(1L)).thenReturn(Optional.of(miembro));
        org.mockito.Mockito.doThrow(new IOException("disco lleno"))
                .when(fileStorageService).deleteFile(anyString());

        assertThatCode(() -> service.delete(1L)).doesNotThrowAnyException();

        verify(miembroDao).delete(miembro);
    }

    @Test
    @DisplayName("delete: sin foto, no intenta borrar ningun archivo")
    void delete_sinFoto_noTocaAlmacenamiento() throws IOException {
        Miembro miembro = new Miembro();
        miembro.setId(2L);
        when(miembroDao.findById(2L)).thenReturn(Optional.of(miembro));

        service.delete(2L);

        verify(fileStorageService, never()).deleteFile(anyString());
        verify(miembroDao).delete(miembro);
    }

    @Test
    @DisplayName("delete: id inexistente lanza NotFoundExceptionResource")
    void delete_idInexistente_lanzaNotFound() {
        when(miembroDao.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(404L)).isInstanceOf(NotFoundExceptionResource.class);
    }

    // ───────────────────────── estado ─────────────────────────

    @Test
    @DisplayName("estado: invierte el estado actual")
    void estado_invierteElEstadoActual() {
        Miembro miembro = new Miembro();
        miembro.setId(1L);
        miembro.setEstado(true);
        when(miembroDao.findById(1L)).thenReturn(Optional.of(miembro));
        when(miembroDao.save(any(Miembro.class))).thenAnswer(inv -> inv.getArgument(0));

        service.estado(1L);

        assertThat(miembro.getEstado()).isFalse();
    }

    @Test
    @DisplayName("estado: id inexistente lanza NotFoundExceptionResource")
    void estado_idInexistente_lanzaNotFound() {
        when(miembroDao.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.estado(404L)).isInstanceOf(NotFoundExceptionResource.class);
    }

    // ───────────────────────── buscarCi ─────────────────────────

    @Test
    @DisplayName("buscarCi: CI inexistente devuelve null, no lanza excepcion")
    void buscarCi_ciInexistente_devuelveNull() {
        when(miembroDao.findByCi("no-existe")).thenReturn(null);

        assertThat(service.buscarCi("no-existe")).isNull();
    }

    // ───────────────────────── update ─────────────────────────

    @Test
    @DisplayName("update: uriFoto se preserva, no se toca desde este metodo")
    void update_preservaUriFotoExistente() {
        Miembro existente = new Miembro();
        existente.setId(1L);
        existente.setUriFoto("original.jpg");
        when(miembroDao.findById(1L)).thenReturn(Optional.of(existente));
        when(miembroDao.save(any(Miembro.class))).thenAnswer(inv -> inv.getArgument(0));

        MiembroDto dto = MiembroDto.builder().id(1L).nombre("Carlos").apellido("Perez").ci("111").build();
        service.update(dto);

        assertThat(existente.getUriFoto()).isEqualTo("original.jpg");
    }

    @Test
    @DisplayName("update: id inexistente lanza NotFoundExceptionResource")
    void update_idInexistente_lanzaNotFound() {
        when(miembroDao.findById(404L)).thenReturn(Optional.empty());

        MiembroDto dto = MiembroDto.builder().id(404L).build();
        assertThatThrownBy(() -> service.update(dto)).isInstanceOf(NotFoundExceptionResource.class);
    }
}
