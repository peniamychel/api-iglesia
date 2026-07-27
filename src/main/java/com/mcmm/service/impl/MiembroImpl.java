package com.mcmm.service.impl;

import com.mcmm.exception.NotFoundExceptionResource;
import com.mcmm.model.dao.MiembroDao;
import com.mcmm.model.dto.MiembroDto.MiembroDto;
import com.mcmm.model.dto.MiembroDto.MiembroImportResultDto;
import com.mcmm.model.dto.MiembroDto.MiembroImportDetalleDto;
import com.mcmm.model.entity.Miembro;
import com.mcmm.service.FileStorageService;
import com.mcmm.service.IMiembro;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.mcmm.model.dao.IglesiaDao;
import com.mcmm.model.dao.MiembroIglesiaDao;
import com.mcmm.model.entity.Iglesia;
import com.mcmm.model.entity.MiembroIglesia;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MiembroImpl implements IMiembro {

    private static final String MIEMBROS_DIR = "miembros/";

    /** Formatos de fecha aceptados al importar celdas de texto en la plantilla. */
    private static final String[] FORMATOS_FECHA = { "yyyy-MM-dd", "dd/MM/yyyy" };

    private final ModelMapper modelMapper;
    private final MiembroDao miembroDao;
    private final FileStorageService fileStorageService;
    private final IglesiaDao iglesiaDao;
    private final MiembroIglesiaDao miembroIglesiaDao;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    @Transactional
    public MiembroDto create(MiembroDto miembroDto) {
        Miembro miembro = modelMapper.map(miembroDto, Miembro.class);
        Miembro savedMiembro = miembroDao.save(miembro);
        return buildDtoWithPhotoUrl(savedMiembro);
    }

    @Override
    @Transactional(readOnly = true)
    public Iterable<MiembroDto> findAll() {
        List<MiembroDto> miembroDtos = new ArrayList<>();
        Iterable<Miembro> miembros = miembroDao.findAll();

        for (Miembro miembro : miembros) {
            miembroDtos.add(buildDtoWithPhotoUrl(miembro));
        }
        return miembroDtos;
    }

    @Override
    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<MiembroDto> findAllPaged(
            String searchText, 
            Boolean estado, 
            String iglesiaNombre, 
            org.springframework.data.domain.Pageable pageable) {
        org.springframework.data.domain.Page<Miembro> miembros = miembroDao.searchMiembros(
                searchText, 
                estado, 
                iglesiaNombre, 
                pageable);
        return miembros.map(this::buildDtoWithPhotoUrl);
    }

    @Override
    @Transactional(readOnly = true)
    public MiembroDto findById(Long id) {
        Miembro miembro = miembroDao.findById(id)
                .orElseThrow(() -> new NotFoundExceptionResource("Miembro", "id", id));
        return buildDtoWithPhotoUrl(miembro);
    }

    @Override
    @Transactional
    public MiembroDto update(MiembroDto miembroDto) {
        Miembro miembroExistente = miembroDao.findById(miembroDto.getId())
                .orElseThrow(() -> new NotFoundExceptionResource("Miembro", "id", miembroDto.getId()));

        miembroExistente.setNombre(miembroDto.getNombre());
        miembroExistente.setApellido(miembroDto.getApellido());
        miembroExistente.setCi(miembroDto.getCi());
        miembroExistente.setFechaNac(miembroDto.getFechaNac());
        miembroExistente.setCelular(miembroDto.getCelular());
        miembroExistente.setSexo(miembroDto.getSexo());
        miembroExistente.setDireccion(miembroDto.getDireccion());
        // uriFoto se preserva — se gestiona mediante endpoints dedicados para subir/eliminar foto
        miembroExistente.setFechaConvercion(miembroDto.getFechaConvercion());
        miembroExistente.setLugarConvercion(miembroDto.getLugarConvercion());
        miembroExistente.setInterventores(miembroDto.getInterventores());
        miembroExistente.setDetalles(miembroDto.getDetalles());
        miembroExistente.setEstado(miembroDto.getEstado());

        Miembro miembroActualizado = miembroDao.save(miembroExistente);
        return buildDtoWithPhotoUrl(miembroActualizado);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Miembro miembro = miembroDao.findById(id)
                .orElseThrow(() -> new NotFoundExceptionResource("Miembro", "id", id));
        
        // Eliminar foto si existe
        if (miembro.getUriFoto() != null && !miembro.getUriFoto().isBlank()) {
            try {
                fileStorageService.deleteFile(MIEMBROS_DIR + miembro.getUriFoto());
            } catch (IOException e) {
                // log error or proceed
            }
        }
        
        miembroDao.delete(miembro);
    }

    @Override
    @Transactional
    public MiembroDto estado(Long id) {
        Miembro miembro = miembroDao.findById(id)
                .orElseThrow(() -> new NotFoundExceptionResource("Miembro", "id", id));
        miembro.setEstado(!miembro.getEstado());
        Miembro updated = miembroDao.save(miembro);
        return buildDtoWithPhotoUrl(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public MiembroDto buscarCi(String ci) {
        Miembro miembro = miembroDao.findByCi(ci);
        if (miembro != null) {
            return buildDtoWithPhotoUrl(miembro);
        }
        return null;
    }

    @Override
    @Transactional
    public String updateProfilePhoto(Long id, MultipartFile file) throws IOException {
        Miembro miembro = miembroDao.findById(id)
                .orElseThrow(() -> new NotFoundExceptionResource("Miembro", "id", id));

        if (miembro.getUriFoto() != null && !miembro.getUriFoto().isBlank()) {
            String uriFoto = miembro.getUriFoto();
            if (!uriFoto.endsWith("/")) {
                String fileNameOnly = uriFoto.substring(uriFoto.lastIndexOf("/") + 1);
                if (!fileNameOnly.isBlank()) {
                    fileStorageService.deleteFile(MIEMBROS_DIR + fileNameOnly);
                }
            }
        }

        String fileName = fileStorageService.storeFile(file, miembro.getNombre(), MIEMBROS_DIR);

        String fileUrl = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path(uploadDir)
                .path("/")
                .path(MIEMBROS_DIR)
                .path(fileName)
                .toUriString();
        miembro.setUriFoto(fileName);
        miembroDao.save(miembro);
        return fileUrl;
    }

    @Override
    @Transactional
    public void deleteProfilePhoto(Long id) {
        Miembro miembro = miembroDao.findById(id)
                .orElseThrow(() -> new NotFoundExceptionResource("Miembro", "id", id));

        if (miembro.getUriFoto() != null && !miembro.getUriFoto().isBlank()) {
            String uriFoto = miembro.getUriFoto();
            if (!uriFoto.endsWith("/")) {
                String fileNameOnly = uriFoto.substring(uriFoto.lastIndexOf("/") + 1);
                if (!fileNameOnly.isBlank()) {
                    try {
                        fileStorageService.deleteFile(MIEMBROS_DIR + fileNameOnly);
                    } catch (IOException e) {
                        throw new RuntimeException("Error al eliminar la foto: " + e.getMessage());
                    }
                }
            }
        }

        miembro.setUriFoto(null);
        miembroDao.save(miembro);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MiembroDto> findSinIglesia() {
        List<MiembroDto> dtos = new ArrayList<>();
        List<Miembro> miembros = miembroDao.findSinIglesia();
        for (Miembro miembro : miembros) {
            dtos.add(buildDtoWithPhotoUrl(miembro));
        }
        return dtos;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MiembroDto> findSinIglesiaParaAsignacion() {
        List<MiembroDto> dtos = new ArrayList<>();
        // El patrón se pasa como parámetro para evitar que '%P' sea interpretado como formato por jboss-logging
        List<Miembro> miembros = miembroDao.findSinIglesiaParaAsignacion("%PASTOR%");
        for (Miembro miembro : miembros) {
            dtos.add(buildDtoWithPhotoUrl(miembro));
        }
        return dtos;
    }

    private MiembroDto buildDtoWithPhotoUrl(Miembro miembro) {
        MiembroDto dto = modelMapper.map(miembro, MiembroDto.class);
        if (dto.getUriFoto() != null) {
            String fileUrl = ServletUriComponentsBuilder
                    .fromCurrentContextPath()
                    .path(uploadDir)
                    .path("/")
                    .path(MIEMBROS_DIR)
                    .path(dto.getUriFoto())
                    .toUriString();
            dto.setUriFoto(fileUrl);
        }
        
        // Cargar nombre de la iglesia activa
        if (miembro.getMiembroIglesias() != null) {
            miembro.getMiembroIglesias().stream()
                .filter(mi -> mi.getEstado() != null && mi.getEstado())
                .findFirst()
                .ifPresent(mi -> {
                    if (mi.getIglesia() != null) {
                        dto.setIglesiaNombre(mi.getIglesia().getNombre());
                    }
                });
        }
        
        // Cargar nombre del cargo activo
        if (miembro.getCargos() != null) {
            miembro.getCargos().stream()
                .filter(c -> c.getEstado() != null && c.getEstado())
                .findFirst()
                .ifPresent(c -> {
                    if (c.getRolCargo() != null) {
                        dto.setCargoNombre(c.getRolCargo().getNombre());
                    }
                });
        }
        
        return dto;
    }

    @Override
    @Transactional
    public MiembroImportResultDto importFromExcel(MultipartFile file, Long iglesiaId) throws IOException {
        Iglesia iglesia = iglesiaDao.findById(iglesiaId)
                .orElseThrow(() -> new NotFoundExceptionResource("Iglesia", "id", iglesiaId));

        List<MiembroImportDetalleDto> importados = new ArrayList<>();
        List<MiembroImportDetalleDto> errores = new ArrayList<>();

        try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;

                FilaParse fp = parseFila(row, formatter);
                if (fp.vacia) continue;

                int fila = row.getRowNum() + 1;
                if (fp.motivo != null) {
                    errores.add(detalle(fila, fp, iglesia.getNombre(), fp.motivo));
                    continue;
                }

                Miembro savedMiembro = miembroDao.save(fp.miembro);
                vincularMiembroIglesia(savedMiembro, iglesia);
                importados.add(detalle(fila, fp, iglesia.getNombre(), null));
            }
        }
        return construirResultado(importados, errores);
    }

    /**
     * Carga masiva del administrador: cada fila indica su iglesia por nombre en la
     * columna "Iglesia" (última columna de la plantilla del admin). Las filas cuya
     * iglesia esté vacía o no coincida con una iglesia activa se rechazan y se detallan.
     */
    @Override
    @Transactional
    public MiembroImportResultDto importFromExcelPorNombreIglesia(MultipartFile file) throws IOException {
        // Índice de iglesias activas por nombre normalizado (trim + minúsculas) para
        // una coincidencia case-insensitive sin ejecutar N consultas.
        java.util.Map<String, Iglesia> iglesiasPorNombre = new java.util.HashMap<>();
        for (Iglesia ig : iglesiaDao.findByEstadoTrue()) {
            if (ig.getNombre() != null) {
                iglesiasPorNombre.put(ig.getNombre().trim().toLowerCase(), ig);
            }
        }

        List<MiembroImportDetalleDto> importados = new ArrayList<>();
        List<MiembroImportDetalleDto> errores = new ArrayList<>();

        try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;

                // La columna Iglesia es la 11 (tras las 11 columnas base 0-10).
                String nombreIglesia = formatter.formatCellValue(row.getCell(11)).trim();
                FilaParse fp = parseFila(row, formatter);

                // Fila totalmente vacía (sin datos de miembro ni iglesia): se ignora.
                if (fp.vacia && nombreIglesia.isEmpty()) continue;

                int fila = row.getRowNum() + 1;
                if (fp.vacia) {
                    errores.add(detalle(fila, fp, nombreIglesia, "Faltan datos del miembro"));
                    continue;
                }
                if (fp.motivo != null) {
                    errores.add(detalle(fila, fp, nombreIglesia, fp.motivo));
                    continue;
                }

                Iglesia iglesia = nombreIglesia.isEmpty() ? null
                        : iglesiasPorNombre.get(nombreIglesia.toLowerCase());
                if (iglesia == null) {
                    errores.add(detalle(fila, fp, nombreIglesia, "Iglesia inválida o faltante"));
                    continue;
                }

                Miembro savedMiembro = miembroDao.save(fp.miembro);
                vincularMiembroIglesia(savedMiembro, iglesia);
                importados.add(detalle(fila, fp, iglesia.getNombre(), null));
            }
        }
        return construirResultado(importados, errores);
    }

    private MiembroImportResultDto construirResultado(List<MiembroImportDetalleDto> importados,
                                                      List<MiembroImportDetalleDto> errores) {
        return MiembroImportResultDto.builder()
                .imported(importados.size())
                .omitidos(errores.size())
                .importados(importados)
                .errores(errores)
                .build();
    }

    private MiembroImportDetalleDto detalle(int fila, FilaParse fp, String iglesia, String motivo) {
        return MiembroImportDetalleDto.builder()
                .fila(fila)
                .ci(fp.ci)
                .nombre(fp.nombre)
                .apellido(fp.apellido)
                .iglesia(iglesia)
                .motivo(motivo)
                .build();
    }

    /**
     * Resultado de intentar parsear una fila de la plantilla (columnas 0-10):
     * fila vacía, error con motivo, o un Miembro válido. Datos del CI/nombre se
     * conservan para poder identificar la fila en el informe.
     */
    private static final class FilaParse {
        final Miembro miembro;   // no nulo solo en éxito
        final String motivo;     // no nulo solo en error
        final boolean vacia;
        final String ci;
        final String nombre;
        final String apellido;

        private FilaParse(Miembro miembro, String motivo, boolean vacia,
                          String ci, String nombre, String apellido) {
            this.miembro = miembro;
            this.motivo = motivo;
            this.vacia = vacia;
            this.ci = ci;
            this.nombre = nombre;
            this.apellido = apellido;
        }

        static FilaParse ok(Miembro m, String ci, String nombre, String apellido) {
            return new FilaParse(m, null, false, ci, nombre, apellido);
        }
        static FilaParse error(String motivo, String ci, String nombre, String apellido) {
            return new FilaParse(null, motivo, false, ci, nombre, apellido);
        }
        static FilaParse vacia() {
            return new FilaParse(null, null, true, "", "", "");
        }
    }

    /**
     * Valida y construye un Miembro a partir de una fila (columnas 0-10), compartido
     * por ambas rutas de importación. Distingue fila vacía, error con motivo, o éxito.
     */
    private FilaParse parseFila(Row row, DataFormatter formatter) {
        String ci = formatter.formatCellValue(row.getCell(0)).trim();
        String nombre = formatter.formatCellValue(row.getCell(1)).trim();
        String apellido = formatter.formatCellValue(row.getCell(2)).trim();

        if (ci.isEmpty() && nombre.isEmpty() && apellido.isEmpty()) {
            return FilaParse.vacia();
        }
        if (ci.isEmpty()) {
            return FilaParse.error("Falta el CI", ci, nombre, apellido);
        }
        if (miembroDao.findByCi(ci) != null) {
            return FilaParse.error("CI ya registrado", ci, nombre, apellido);
        }
        if (nombre.isEmpty() || apellido.isEmpty()) {
            return FilaParse.error("Falta nombre o apellido", ci, nombre, apellido);
        }

        Miembro miembro = new Miembro();
        miembro.setCi(ci);
        miembro.setNombre(nombre);
        miembro.setApellido(apellido);
        miembro.setFechaNac(parseFechaCelda(row.getCell(3), formatter));
        miembro.setCelular(formatter.formatCellValue(row.getCell(4)).trim());
        miembro.setSexo(normalizarSexo(formatter.formatCellValue(row.getCell(5))));
        miembro.setDireccion(formatter.formatCellValue(row.getCell(6)).trim());
        miembro.setFechaConvercion(parseFechaCelda(row.getCell(7), formatter));
        miembro.setLugarConvercion(formatter.formatCellValue(row.getCell(8)).trim());
        miembro.setInterventores(formatter.formatCellValue(row.getCell(9)).trim());
        miembro.setDetalles(formatter.formatCellValue(row.getCell(10)).trim());
        miembro.setEstado(true);
        return FilaParse.ok(miembro, ci, nombre, apellido);
    }

    /**
     * Lee una fecha de una celda de la plantilla, tolerante al formato:
     *  1) Si Excel la guardó como fecha real (numérica con formato de fecha) —el caso
     *     típico al escribir "1995-04-25" y que Excel la convierta a "25/04/1995"—
     *     se lee su valor verdadero con getDateCellValue(), sin importar cómo se muestre.
     *  2) Si quedó como texto, se intentan los formatos aceptados (AAAA-MM-DD y DD/MM/AAAA).
     * Devuelve {@code null} si la celda está vacía o no coincide con ningún formato,
     * de modo que una fecha inválida no aborta la importación del miembro.
     */
    private Date parseFechaCelda(Cell cell, DataFormatter formatter) {
        if (cell == null) return null;
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            return cell.getDateCellValue();
        }
        String val = formatter.formatCellValue(cell).trim();
        if (val.isEmpty()) return null;
        for (String patron : FORMATOS_FECHA) {
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat(patron);
                sdf.setLenient(false);
                return sdf.parse(val);
            } catch (java.text.ParseException ignored) { }
        }
        return null;
    }

    /**
     * Normaliza el sexo al estándar que usa el resto del sistema y la BD:
     * "Hombre" / "Mujer". Acepta abreviaturas (M/F, H) y palabras completas en
     * distintas capitalizaciones. Si el valor no se reconoce, se conserva tal cual
     * para no perder el dato.
     */
    private String normalizarSexo(String raw) {
        if (raw == null) return null;
        String v = raw.trim();
        if (v.isEmpty()) return v;
        switch (v.toLowerCase()) {
            case "m":
            case "h":
            case "masculino":
            case "hombre":
            case "varon":
            case "varón":
                return "Hombre";
            case "f":
            case "femenino":
            case "mujer":
                return "Mujer";
            default:
                return v;
        }
    }

    /** Vincula un miembro a una iglesia mediante un registro activo en MiembroIglesia. */
    private void vincularMiembroIglesia(Miembro miembro, Iglesia iglesia) {
        MiembroIglesia mi = new MiembroIglesia();
        mi.setMiembro(miembro);
        mi.setIglesia(iglesia);
        mi.setFecha(new Date());
        mi.setEstado(true);
        miembroIglesiaDao.save(mi);
    }

    @Override
    public byte[] generateExcelTemplate(boolean includeIglesia) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Plantilla Miembros");

            // Header font and style
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);

            // Headers base (columnas 0-10)
            java.util.List<String> headerList = new java.util.ArrayList<>(java.util.Arrays.asList(
                "CI", "Nombre", "Apellido", "Fecha Nacimiento (DD/MM/AAAA)",
                "Celular", "Sexo (M/F)", "Direccion",
                "Fecha Conversion (DD/MM/AAAA)", "Lugar Conversion",
                "Interventores", "Detalles"
            ));
            // Columna Iglesia solo para el admin (índice 11): cada miembro se asigna
            // a su iglesia por nombre exacto.
            if (includeIglesia) {
                headerList.add("Iglesia (nombre exacto)");
            }
            String[] headers = headerList.toArray(new String[0]);

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Sample data row
            Row sampleRow = sheet.createRow(1);
            sampleRow.createCell(0).setCellValue("12345678");
            sampleRow.createCell(1).setCellValue("Juan");
            sampleRow.createCell(2).setCellValue("Pérez");
            sampleRow.createCell(3).setCellValue("25/04/1995");
            sampleRow.createCell(4).setCellValue("78945612");
            sampleRow.createCell(5).setCellValue("M");
            sampleRow.createCell(6).setCellValue("Av. Principal #123");
            sampleRow.createCell(7).setCellValue("12/09/2018");
            sampleRow.createCell(8).setCellValue("Templo Central");
            sampleRow.createCell(9).setCellValue("Pastor Carlos Gomez");
            sampleRow.createCell(10).setCellValue("Ejemplo de registro");
            if (includeIglesia) {
                // Usa el nombre de una iglesia activa real como ejemplo, si existe.
                String ejemplo = iglesiaDao.findByEstadoTrue().stream()
                        .map(Iglesia::getNombre)
                        .filter(java.util.Objects::nonNull)
                        .findFirst()
                        .orElse("Nombre exacto de la iglesia");
                sampleRow.createCell(11).setCellValue(ejemplo);
            }

            // Auto size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }
}

