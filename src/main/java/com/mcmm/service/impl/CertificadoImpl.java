package com.mcmm.service.impl;

import com.mcmm.exception.NotFoundExceptionResource;
import com.mcmm.model.dto.certificado.CertificadoDto;
import com.mcmm.model.entity.Certificado;
import com.mcmm.model.entity.Evento;
import com.mcmm.model.dao.CertificadoDao;
import com.mcmm.model.dao.EventoDao;
import com.mcmm.model.dao.PlantillaCertificadoRepository;
import com.mcmm.model.dao.ParticipacionEventoDao;
import com.mcmm.service.FileStorageService;
import com.mcmm.service.ICertificado;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Service
@RequiredArgsConstructor
@Slf4j
public class CertificadoImpl implements ICertificado {

    private static final String CERTIFICADOS_DIR = "certificados/";

    private final CertificadoDao certificadoDao;
    private final EventoDao eventoDao;
    private final PlantillaCertificadoRepository plantillaCertificadoDao;
    private final ParticipacionEventoDao participacionEventoDao;
    private final ModelMapper modelMapper;
    private final FileStorageService fileStorageService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    private Long getCurrentIglesiaId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getDetails() instanceof Map) {
            Map<?, ?> details = (Map<?, ?>) authentication.getDetails();
            Object iglesiaIdObj = details.get("iglesiaId");
            if (iglesiaIdObj instanceof Long) {
                return (Long) iglesiaIdObj;
            } else if (iglesiaIdObj instanceof Integer) {
                return ((Integer) iglesiaIdObj).longValue();
            }
        }
        return null;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CertificadoDto> findAll() {
        Long iglesiaId = getCurrentIglesiaId();
        List<Certificado> certificados;
        if (iglesiaId != null) {
            certificados = certificadoDao.findByEventoIglesiaId(iglesiaId);
        } else {
            certificados = StreamSupport.stream(certificadoDao.findAll().spliterator(), false)
                    .collect(Collectors.toList());
        }
        return certificados.stream()
                .map(this::buildDtoWithPhotoUrl)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CertificadoDto findById(Long id) {
        Certificado certificado = certificadoDao.findById(id)
                .orElseThrow(() -> new NotFoundExceptionResource("Certificado", "id", id));
        return buildDtoWithPhotoUrl(certificado);
    }

    @Override
    @Transactional
    public CertificadoDto create(CertificadoDto certificadoDto) {
        Certificado certificado = modelMapper.map(certificadoDto, Certificado.class);
        if (certificadoDto.getEventoId() != null) {
            Evento evento = eventoDao.findById(certificadoDto.getEventoId()).orElse(null);
            certificado.setEvento(evento);
        }
        if (certificadoDto.getPlantillaCertificadoId() != null) {
            com.mcmm.model.entity.PlantillaCertificado plantilla = plantillaCertificadoDao.findById(certificadoDto.getPlantillaCertificadoId()).orElse(null);
            certificado.setPlantillaCertificado(plantilla);
        }
        Certificado saved = certificadoDao.save(certificado);
        return buildDtoWithPhotoUrl(saved);
    }

    @Override
    @Transactional
    public CertificadoDto update(CertificadoDto certificadoDto) {
        Certificado certificado = certificadoDao.findById(certificadoDto.getId())
                .orElseThrow(() -> new NotFoundExceptionResource("Certificado", "id", certificadoDto.getId()));
        certificado.setMotivoCertificado(certificadoDto.getMotivoCertificado());
        certificado.setEstado(certificadoDto.getEstado());
        if (certificadoDto.getEventoId() != null) {
            Evento evento = eventoDao.findById(certificadoDto.getEventoId()).orElse(null);
            certificado.setEvento(evento);
        }
        if (certificadoDto.getPlantillaCertificadoId() != null) {
            com.mcmm.model.entity.PlantillaCertificado plantilla = plantillaCertificadoDao.findById(certificadoDto.getPlantillaCertificadoId()).orElse(null);
            certificado.setPlantillaCertificado(plantilla);
        }
        Certificado saved = certificadoDao.save(certificado);
        return buildDtoWithPhotoUrl(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Certificado certificado = certificadoDao.findById(id)
                .orElseThrow(() -> new NotFoundExceptionResource("Certificado", "id", id));
                
        if (certificado.getUriFoto() != null && !certificado.getUriFoto().isBlank()) {
            try {
                fileStorageService.deleteFile(CERTIFICADOS_DIR + certificado.getUriFoto());
            } catch (IOException e) {
                throw new RuntimeException("Error al eliminar la foto: " + e.getMessage());
            }
        }
        
        com.mcmm.model.entity.PlantillaCertificado plantilla = certificado.getPlantillaCertificado();
        
        // Detach the certificate from any participations to avoid foreign key constraints
        participacionEventoDao.detachCertificado(id);
        
        // Remove reference first to avoid foreign key issues
        certificado.setPlantillaCertificado(null);
        certificadoDao.delete(certificado);
        
        if (plantilla != null) {
            // La plantilla ya no guarda imágenes, así que solo se borra el registro.
            plantillaCertificadoDao.delete(plantilla);
        }
    }

    @Override
    @Transactional
    public void estado(Long id) {
        certificadoDao.toggleEstado(id);
    }

    @Override
    @Transactional
    public String uploadProfilePhoto(Long id, MultipartFile file) throws IOException {
        Certificado certificado = certificadoDao.findById(id)
                .orElseThrow(() -> new NotFoundExceptionResource("Certificado", "id", id));

        if (certificado.getUriFoto() != null && !certificado.getUriFoto().isBlank()) {
            fileStorageService.deleteFile(CERTIFICADOS_DIR + certificado.getUriFoto());
        }

        String fileName = fileStorageService.storeFile(file, "certificado", CERTIFICADOS_DIR);

        String fileUrl = ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path("/uploads/")
                .path(CERTIFICADOS_DIR)
                .path(fileName)
                .toUriString();
        certificado.setUriFoto(fileName);
        certificadoDao.save(certificado);
        return fileUrl;
    }

    @Override
    @Transactional
    public void deleteProfilePhoto(Long id) {
        Certificado certificado = certificadoDao.findById(id)
                .orElseThrow(() -> new NotFoundExceptionResource("Certificado", "id", id));

        if (certificado.getUriFoto() != null && !certificado.getUriFoto().isBlank()) {
            try {
                fileStorageService.deleteFile(CERTIFICADOS_DIR + certificado.getUriFoto());
            } catch (IOException e) {
                throw new RuntimeException("Error al eliminar la foto: " + e.getMessage());
            }
        }

        certificado.setUriFoto(null);
        certificadoDao.save(certificado);
    }

    private CertificadoDto buildDtoWithPhotoUrl(Certificado certificado) {
        CertificadoDto dto = modelMapper.map(certificado, CertificadoDto.class);
        if (certificado.getEvento() != null) {
            dto.setEventoId(certificado.getEvento().getId());
        }
        if (certificado.getPlantillaCertificado() != null) {
            dto.setPlantillaCertificadoId(certificado.getPlantillaCertificado().getId());
        }
        if (dto.getUriFoto() != null) {
            String fileUrl = ServletUriComponentsBuilder
                    .fromCurrentContextPath()
                    .path("/uploads/")
                    .path(CERTIFICADOS_DIR)
                    .path(dto.getUriFoto())
                    .toUriString();
            dto.setUriFoto(fileUrl);
        }
        return dto;
    }
}
