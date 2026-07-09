package com.mcmm.service.impl;

import com.mcmm.exception.NotFoundExceptionResource;
import com.mcmm.model.dao.MiembroDao;
import com.mcmm.model.dto.MiembroDto.MiembroDto;
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
    public int importFromExcel(MultipartFile file, Long iglesiaId) throws IOException {
        int count = 0;
        Iglesia iglesia = iglesiaDao.findById(iglesiaId)
                .orElseThrow(() -> new NotFoundExceptionResource("Iglesia", "id", iglesiaId));

        try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;

                String ci = formatter.formatCellValue(row.getCell(0)).trim();
                if (ci.isEmpty()) continue;

                if (miembroDao.findByCi(ci) != null) {
                    continue;
                }

                String nombre = formatter.formatCellValue(row.getCell(1)).trim();
                String apellido = formatter.formatCellValue(row.getCell(2)).trim();
                if (nombre.isEmpty() || apellido.isEmpty()) continue;

                Miembro miembro = new Miembro();
                miembro.setCi(ci);
                miembro.setNombre(nombre);
                miembro.setApellido(apellido);
                
                try {
                    Cell cellNac = row.getCell(3);
                    if (cellNac != null) {
                        if (cellNac.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cellNac)) {
                            miembro.setFechaNac(cellNac.getDateCellValue());
                        } else {
                            String cellVal = formatter.formatCellValue(cellNac).trim();
                            if (!cellVal.isEmpty()) {
                                miembro.setFechaNac(new java.text.SimpleDateFormat("yyyy-MM-dd").parse(cellVal));
                            }
                        }
                    }
                } catch (Exception e) {}

                miembro.setCelular(formatter.formatCellValue(row.getCell(4)).trim());
                miembro.setSexo(formatter.formatCellValue(row.getCell(5)).trim());
                miembro.setDireccion(formatter.formatCellValue(row.getCell(6)).trim());

                try {
                    Cell cellConv = row.getCell(7);
                    if (cellConv != null) {
                        if (cellConv.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cellConv)) {
                            miembro.setFechaConvercion(cellConv.getDateCellValue());
                        } else {
                            String cellVal = formatter.formatCellValue(cellConv).trim();
                            if (!cellVal.isEmpty()) {
                                miembro.setFechaConvercion(new java.text.SimpleDateFormat("yyyy-MM-dd").parse(cellVal));
                            }
                        }
                    }
                } catch (Exception e) {}

                miembro.setLugarConvercion(formatter.formatCellValue(row.getCell(8)).trim());
                miembro.setInterventores(formatter.formatCellValue(row.getCell(9)).trim());
                miembro.setDetalles(formatter.formatCellValue(row.getCell(10)).trim());
                miembro.setEstado(true);

                Miembro savedMiembro = miembroDao.save(miembro);

                MiembroIglesia mi = new MiembroIglesia();
                mi.setMiembro(savedMiembro);
                mi.setIglesia(iglesia);
                mi.setFecha(new Date());
                mi.setEstado(true);
                miembroIglesiaDao.save(mi);

                count++;
            }
        }
        return count;
    }

    @Override
    public byte[] generateExcelTemplate() throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Plantilla Miembros");
            
            // Header font and style
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);
            
            // Headers
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                "CI", "Nombre", "Apellido", "Fecha Nacimiento (AAAA-MM-DD)", 
                "Celular", "Sexo (M/F)", "Direccion", 
                "Fecha Conversion (AAAA-MM-DD)", "Lugar Conversion", 
                "Interventores", "Detalles"
            };
            
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
            sampleRow.createCell(3).setCellValue("1995-04-25");
            sampleRow.createCell(4).setCellValue("78945612");
            sampleRow.createCell(5).setCellValue("M");
            sampleRow.createCell(6).setCellValue("Av. Principal #123");
            sampleRow.createCell(7).setCellValue("2018-09-12");
            sampleRow.createCell(8).setCellValue("Templo Central");
            sampleRow.createCell(9).setCellValue("Pastor Carlos Gomez");
            sampleRow.createCell(10).setCellValue("Ejemplo de registro");
            
            // Auto size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            
            workbook.write(out);
            return out.toByteArray();
        }
    }
}

