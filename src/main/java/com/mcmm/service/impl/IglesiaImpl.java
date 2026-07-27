package com.mcmm.service.impl;

import com.mcmm.exception.NotFoundExceptionResource;
import com.mcmm.model.dao.IglesiaDao;
import com.mcmm.model.dto.iglesiaDto.IglesiaDto;
import com.mcmm.model.entity.Iglesia;
import com.mcmm.service.FileStorageService;
import com.mcmm.service.IIglesia;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class IglesiaImpl implements IIglesia {

    private static final String IGLESIAS_DIR = "iglesias/";

    private final ModelMapper modelMapper;
    private final IglesiaDao iglesiaDao;
    private final FileStorageService fileStorageService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    @Transactional
    public IglesiaDto save(IglesiaDto iglesiaDto) {
        Iglesia iglesia = modelMapper.map(iglesiaDto, Iglesia.class);
        Iglesia savedIglesia = iglesiaDao.save(iglesia);
        return buildDtoWithPhotoUrl(savedIglesia);
    }

    @Override
    @Transactional(readOnly = true)
    public List<IglesiaDto> findAll() {
        return StreamSupport.stream(iglesiaDao.findAllOrderByOrdenAscAndCreatedAtDesc().spliterator(), false)
                .map(this::buildDtoWithPhotoUrl)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public IglesiaDto findById(Long id) {
        Iglesia iglesia = iglesiaDao.findById(id)
                .orElseThrow(() -> new NotFoundExceptionResource("Iglesia", "id", id));
        return buildDtoWithPhotoUrl(iglesia);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Iglesia iglesia = iglesiaDao.findById(id)
                .orElseThrow(() -> new NotFoundExceptionResource("Iglesia", "id", id));
        // Eliminar foto si existe
        if (iglesia.getUriFoto() != null && !iglesia.getUriFoto().isBlank()) {
            try {
                fileStorageService.deleteFile(IGLESIAS_DIR + iglesia.getUriFoto());
            } catch (IOException e) {
                // Continuar aunque no se pueda borrar el archivo
            }
        }
        iglesiaDao.delete(iglesia);
    }

    @Override
    @Transactional
    public IglesiaDto update(Long id, IglesiaDto iglesiaDto) {
        Iglesia iglesia = iglesiaDao.findById(id)
                .orElseThrow(() -> new NotFoundExceptionResource("Iglesia", "id", id));
        iglesia.setNombre(iglesiaDto.getNombre());
        iglesia.setDireccion(iglesiaDto.getDireccion());
        iglesia.setTelefono(iglesiaDto.getTelefono());
        iglesia.setFechaFundacion(iglesiaDto.getFechaFundacion());
        iglesia.setEstado(iglesiaDto.getEstado());
        iglesia.setLatitud(iglesiaDto.getLatitud());
        iglesia.setLongitud(iglesiaDto.getLongitud());
        // uriFoto se preserva — no se toca aquí
        Iglesia updated = iglesiaDao.save(iglesia);
        return buildDtoWithPhotoUrl(updated);
    }

    @Override
    @Transactional
    public IglesiaDto estado(Long id) {
        Iglesia iglesia = iglesiaDao.findById(id)
                .orElseThrow(() -> new NotFoundExceptionResource("Iglesia", "id", id));
        iglesia.setEstado(!iglesia.getEstado());
        Iglesia updated = iglesiaDao.save(iglesia);
        return buildDtoWithPhotoUrl(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public IglesiaDto buscarNombreIglesia(String nameIglesia) {
        Iglesia iglesia = iglesiaDao.buscarPorNombreIglesia(nameIglesia);
        if (iglesia != null) {
            return buildDtoWithPhotoUrl(iglesia);
        }
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public IglesiaDto buscarNombreIglesiaExceptoId(Long id, String nameIglesia) {
        Iglesia iglesia = iglesiaDao.buscarPorNombreIglesiaExceptoId(id, nameIglesia);
        if (iglesia != null) {
            return buildDtoWithPhotoUrl(iglesia);
        }
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<IglesiaDto> findByEstadoTrue() {
        return iglesiaDao.findByEstadoTrue().stream()
                .map(this::buildDtoWithPhotoUrl)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public IglesiaDto findByNombreAndIdNot(String nameIglesia, Long id) {
        Iglesia iglesia = iglesiaDao.findByNombreAndIdNot(nameIglesia, id);
        if (iglesia != null) {
            return buildDtoWithPhotoUrl(iglesia);
        }
        return null;
    }

    @Override
    @Transactional
    public String updateFoto(Long id, MultipartFile file) throws IOException {
        Iglesia iglesia = iglesiaDao.findById(id)
                .orElseThrow(() -> new NotFoundExceptionResource("Iglesia", "id", id));

        // Eliminar foto anterior si existe
        if (iglesia.getUriFoto() != null && !iglesia.getUriFoto().isBlank()) {
            String existing = iglesia.getUriFoto();
            if (!existing.endsWith("/")) {
                String fileNameOnly = existing.substring(existing.lastIndexOf("/") + 1);
                if (!fileNameOnly.isBlank()) {
                    fileStorageService.deleteFile(IGLESIAS_DIR + fileNameOnly);
                }
            }
        }

        String fileName = fileStorageService.storeFile(file, iglesia.getNombre(), IGLESIAS_DIR);

        String fileUrl = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path(uploadDir)
                .path("/")
                .path(IGLESIAS_DIR)
                .path(fileName)
                .toUriString();

        iglesia.setUriFoto(fileName);
        iglesiaDao.save(iglesia);
        return fileUrl;
    }

    @Override
    @Transactional
    public void deleteFoto(Long id) {
        Iglesia iglesia = iglesiaDao.findById(id)
                .orElseThrow(() -> new NotFoundExceptionResource("Iglesia", "id", id));

        if (iglesia.getUriFoto() != null && !iglesia.getUriFoto().isBlank()) {
            String existing = iglesia.getUriFoto();
            if (!existing.endsWith("/")) {
                String fileNameOnly = existing.substring(existing.lastIndexOf("/") + 1);
                if (!fileNameOnly.isBlank()) {
                    try {
                        fileStorageService.deleteFile(IGLESIAS_DIR + fileNameOnly);
                    } catch (IOException e) {
                        throw new RuntimeException("Error al eliminar la foto: " + e.getMessage());
                    }
                }
            }
        }

        iglesia.setUriFoto(null);
        iglesiaDao.save(iglesia);
    }

    @Override
    @Transactional
    public void updateOrden(List<Long> ids) {
        for (int i = 0; i < ids.size(); i++) {
            Long id = ids.get(i);
            int finalI = i;
            iglesiaDao.findById(id).ifPresent(iglesia -> {
                iglesia.setOrden(finalI + 1);
                iglesiaDao.save(iglesia);
            });
        }
    }

    private IglesiaDto buildDtoWithPhotoUrl(Iglesia iglesia) {
        IglesiaDto dto = modelMapper.map(iglesia, IglesiaDto.class);
        if (dto.getUriFoto() != null && !dto.getUriFoto().isBlank()) {
            String fileUrl = ServletUriComponentsBuilder
                    .fromCurrentContextPath()
                    .path(uploadDir)
                    .path("/")
                    .path(IGLESIAS_DIR)
                    .path(dto.getUriFoto())
                    .toUriString();
            dto.setUriFoto(fileUrl);
        }
        return dto;
    }
}
