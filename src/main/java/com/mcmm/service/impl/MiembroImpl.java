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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MiembroImpl implements IMiembro {

    private static final String MIEMBROS_DIR = "miembros/";

    private final ModelMapper modelMapper;
    private final MiembroDao miembroDao;
    private final FileStorageService fileStorageService;

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
        miembroExistente.setUriFoto(miembroDto.getUriFoto());
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
        return dto;
    }
}
