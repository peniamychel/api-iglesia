package com.mcmm.service.impl;

import com.mcmm.exception.NotFoundExceptionResource;
import com.mcmm.model.dao.IglesiaDao;
import com.mcmm.model.dao.MiembroDao;
import com.mcmm.model.dao.MiembroIglesiaDao;
import com.mcmm.model.dto.GraficoDataDto;
import com.mcmm.model.dto.MiembroDto.MiembroDto;
import com.mcmm.model.dto.MiembroIglesiaDto;
import com.mcmm.model.entity.Iglesia;
import com.mcmm.model.entity.Miembro;
import com.mcmm.model.entity.MiembroIglesia;
import com.mcmm.service.IMiembroIglesia;
import com.mcmm.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class MiembroIglesiaImpl implements IMiembroIglesia {

    private static final String CARTAS_DIR = "cartas-traspaso/";

    private final ModelMapper modelMapper;
    private final MiembroIglesiaDao miembroIglesiaDao;
    private final MiembroDao miembroDao;
    private final IglesiaDao iglesiaDao;
    private final FileStorageService fileStorageService;

    @Value("${file.upload-dir}")
    private String uploadDir;

    private MiembroIglesiaDto convertToDto(MiembroIglesia entity) {
        MiembroIglesiaDto dto = modelMapper.map(entity, MiembroIglesiaDto.class);
        if (entity.getMiembro() != null) {
            dto.setMiembroId(entity.getMiembro().getId());
        }
        if (entity.getIglesia() != null) {
            dto.setIglesiaId(entity.getIglesia().getId());
        }
        if (entity.getIglesiaDestino() != null) {
            dto.setIglesiaDestinoId(entity.getIglesiaDestino().getId());
        }
        if (dto.getUriCartaTraspaso() != null && !dto.getUriCartaTraspaso().isBlank() && !dto.getUriCartaTraspaso().startsWith("http")) {
            String fileUrl = org.springframework.web.servlet.support.ServletUriComponentsBuilder
                    .fromCurrentContextPath()
                    .path(uploadDir)
                    .path("/")
                    .path(CARTAS_DIR)
                    .path(dto.getUriCartaTraspaso())
                    .toUriString();
            dto.setUriCartaTraspaso(fileUrl);
        }
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MiembroIglesiaDto> findAll() {
        return StreamSupport.stream(miembroIglesiaDao.findAll().spliterator(), false)
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MiembroIglesiaDto findById(Long id) {
        MiembroIglesia miembroIglesia = miembroIglesiaDao.findById(id)
                .orElseThrow(() -> new NotFoundExceptionResource("MiembroIglesia", "id", id));
        return convertToDto(miembroIglesia);
    }

    @Override
    @Transactional
    public MiembroIglesiaDto save(MiembroIglesiaDto miembroIglesiaDto) {
        Miembro miembro = miembroDao.findById(miembroIglesiaDto.getMiembroId())
                .orElseThrow(() -> new NotFoundExceptionResource("Miembro", "id", miembroIglesiaDto.getMiembroId()));
        Iglesia iglesia = iglesiaDao.findById(miembroIglesiaDto.getIglesiaId())
                .orElseThrow(() -> new NotFoundExceptionResource("Iglesia", "id", miembroIglesiaDto.getIglesiaId()));

        // Desactivar asignación activa previa del miembro
        miembroIglesiaDao.findActiveByMiembroId(miembro.getId()).ifPresent(active -> {
            active.setEstado(false);
            miembroIglesiaDao.save(active);
        });

        MiembroIglesia miembroIglesia = modelMapper.map(miembroIglesiaDto, MiembroIglesia.class);
        miembroIglesia.setMiembro(miembro);
        miembroIglesia.setIglesia(iglesia);
        miembroIglesia.setEstado(true);

        MiembroIglesia saved = miembroIglesiaDao.save(miembroIglesia);
        return convertToDto(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        MiembroIglesia miembroIglesia = miembroIglesiaDao.findById(id)
                .orElseThrow(() -> new NotFoundExceptionResource("MiembroIglesia", "id", id));
        miembroIglesiaDao.delete(miembroIglesia);
    }

    @Override
    @Transactional
    public MiembroIglesiaDto update(MiembroIglesiaDto miembroIglesiaDto) {
        MiembroIglesia miembroIglesiaE = miembroIglesiaDao.findById(miembroIglesiaDto.getId())
                .orElseThrow(() -> new NotFoundExceptionResource("MiembroIglesia", "id", miembroIglesiaDto.getId()));

        if (miembroIglesiaDto.getMiembroId() != null) {
            Miembro miembro = miembroDao.findById(miembroIglesiaDto.getMiembroId())
                    .orElseThrow(() -> new NotFoundExceptionResource("Miembro", "id", miembroIglesiaDto.getMiembroId()));
            miembroIglesiaE.setMiembro(miembro);
        }

        if (miembroIglesiaDto.getIglesiaId() != null) {
            Iglesia iglesia = iglesiaDao.findById(miembroIglesiaDto.getIglesiaId())
                    .orElseThrow(() -> new NotFoundExceptionResource("Iglesia", "id", miembroIglesiaDto.getIglesiaId()));
            miembroIglesiaE.setIglesia(iglesia);
        }

        if (miembroIglesiaDto.getIglesiaDestinoId() != null) {
            Iglesia iglesiaDest = iglesiaDao.findById(miembroIglesiaDto.getIglesiaDestinoId())
                    .orElseThrow(() -> new NotFoundExceptionResource("Iglesia", "id", miembroIglesiaDto.getIglesiaDestinoId()));
            miembroIglesiaE.setIglesiaDestino(iglesiaDest);
        }

        miembroIglesiaE.setFecha(miembroIglesiaDto.getFecha());
        miembroIglesiaE.setMotivoTraspaso(miembroIglesiaDto.getMotivoTraspaso());
        miembroIglesiaE.setFechaTraspaso(miembroIglesiaDto.getFechaTraspaso());
        miembroIglesiaE.setUriCartaTraspaso(miembroIglesiaDto.getUriCartaTraspaso());
        miembroIglesiaE.setEstado(miembroIglesiaDto.getEstado());
        miembroIglesiaE.setEstadoTraspaso(miembroIglesiaDto.getEstadoTraspaso());

        MiembroIglesia updated = miembroIglesiaDao.save(miembroIglesiaE);
        return convertToDto(updated);
    }

    @Override
    @Transactional
    public MiembroIglesiaDto estado(Long id) {
        MiembroIglesia miembroIglesiaE = miembroIglesiaDao.findById(id)
                .orElseThrow(() -> new NotFoundExceptionResource("MiembroIglesia", "id", id));
        miembroIglesiaE.setEstado(!miembroIglesiaE.getEstado());
        MiembroIglesia updated = miembroIglesiaDao.save(miembroIglesiaE);
        return convertToDto(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MiembroDto> findMiembrosIglesia(Long id) {
        return miembroIglesiaDao.findMiembrosIglesia(id).stream()
                .map(miembro -> {
                    MiembroDto dto = modelMapper.map(miembro, MiembroDto.class);
                    if (dto.getUriFoto() != null && !dto.getUriFoto().isBlank()) {
                        String fileUrl = org.springframework.web.servlet.support.ServletUriComponentsBuilder
                                .fromCurrentContextPath()
                                .path(uploadDir)
                                .path("/")
                                .path("miembros/")
                                .path(dto.getUriFoto())
                                .toUriString();
                        dto.setUriFoto(fileUrl);
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
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean findByIdMiembro(Long id) {
        return miembroIglesiaDao.findByMiembro(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GraficoDataDto> graficoMiembrosIglesia(Long limite) {
        List<Object[]> resultados = miembroIglesiaDao.obtenerIglesiasConMasMiembros(limite);
        return resultados.stream().map(fila -> {
            GraficoDataDto dto = new GraficoDataDto();
            dto.setId((Long) fila[0]);
            dto.setNombre((String) fila[1] + " - " + (String) fila[2]);
            dto.setValor((Long) fila[3]);
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MiembroIglesiaDto solicitarTraspaso(MiembroIglesiaDto dto) {
        if (miembroIglesiaDao.existsByMiembroIdAndEstadoTraspasoPending(dto.getMiembroId())) {
            throw new IllegalArgumentException("El miembro ya cuenta con un proceso de traspaso pendiente.");
        }

        MiembroIglesia active = miembroIglesiaDao.findActiveByMiembroId(dto.getMiembroId())
                .orElseThrow(() -> new IllegalArgumentException("El miembro no está asignado a ninguna iglesia actualmente."));

        Iglesia destino = iglesiaDao.findById(dto.getIglesiaDestinoId())
                .orElseThrow(() -> new NotFoundExceptionResource("Iglesia", "id", dto.getIglesiaDestinoId()));

        active.setIglesiaDestino(destino);
        active.setEstadoTraspaso("PENDIENTE");
        active.setMotivoTraspaso(dto.getMotivoTraspaso());
        active.setUriCartaTraspaso(dto.getUriCartaTraspaso());
        active.setFechaTraspaso(dto.getFechaTraspaso() != null ? dto.getFechaTraspaso() : new java.util.Date());

        MiembroIglesia saved = miembroIglesiaDao.save(active);
        return convertToDto(saved);
    }

    @Override
    @Transactional
    public MiembroIglesiaDto aceptarTraspaso(Long id) {
        MiembroIglesia solicitud = miembroIglesiaDao.findById(id)
                .orElseThrow(() -> new NotFoundExceptionResource("MiembroIglesia", "id", id));

        if (!"PENDIENTE".equals(solicitud.getEstadoTraspaso())) {
            throw new IllegalArgumentException("La solicitud de traspaso no está pendiente.");
        }

        // 1. Desactivar asignación anterior
        solicitud.setEstado(false);
        solicitud.setEstadoTraspaso("ACEPTADO");
        solicitud.setFechaTraspaso(new java.util.Date());
        miembroIglesiaDao.save(solicitud);

        // 2. Crear nueva asignación activa en la iglesia destino
        MiembroIglesia nuevaAsignacion = new MiembroIglesia();
        nuevaAsignacion.setMiembro(solicitud.getMiembro());
        nuevaAsignacion.setIglesia(solicitud.getIglesiaDestino());
        nuevaAsignacion.setFecha(new java.util.Date());
        nuevaAsignacion.setEstado(true);

        MiembroIglesia saved = miembroIglesiaDao.save(nuevaAsignacion);
        return convertToDto(saved);
    }

    @Override
    @Transactional
    public MiembroIglesiaDto rechazarTraspaso(Long id) {
        MiembroIglesia solicitud = miembroIglesiaDao.findById(id)
                .orElseThrow(() -> new NotFoundExceptionResource("MiembroIglesia", "id", id));

        if (!"PENDIENTE".equals(solicitud.getEstadoTraspaso())) {
            throw new IllegalArgumentException("La solicitud de traspaso no está pendiente.");
        }

        solicitud.setEstadoTraspaso("RECHAZADO");
        solicitud.setIglesiaDestino(null);
        solicitud.setMotivoTraspaso(solicitud.getMotivoTraspaso() + " (RECHAZADO)");

        MiembroIglesia saved = miembroIglesiaDao.save(solicitud);
        return convertToDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MiembroIglesiaDto> getSolicitudesPendientes(Long iglesiaId) {
        if (iglesiaId == null || iglesiaId == 0) {
            return miembroIglesiaDao.findAllPendingTransfers().stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        }
        return miembroIglesiaDao.findPendingTransfersForChurch(iglesiaId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MiembroIglesiaDto> obtenerHistorialMiembro(Long miembroId) {
        return miembroIglesiaDao.findHistorialByMiembroId(miembroId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public String subirCartaTraspaso(Long id, MultipartFile file) throws IOException {
        MiembroIglesia active = miembroIglesiaDao.findById(id)
                .orElseThrow(() -> new NotFoundExceptionResource("MiembroIglesia", "id", id));

        // If there's an existing file, delete it
        if (active.getUriCartaTraspaso() != null && !active.getUriCartaTraspaso().isBlank()) {
            try {
                fileStorageService.deleteFile(CARTAS_DIR + active.getUriCartaTraspaso());
            } catch (IOException e) {
                // proceed
            }
        }

        // Store new file
        String fileName = fileStorageService.storeFile(file, active.getMiembro().getNombre() + "_" + active.getMiembro().getApellido(), CARTAS_DIR);
        active.setUriCartaTraspaso(fileName);
        miembroIglesiaDao.save(active);

        // Return public URL
        return org.springframework.web.servlet.support.ServletUriComponentsBuilder
                .fromCurrentContextPath()
                .path(uploadDir)
                .path("/")
                .path(CARTAS_DIR)
                .path(fileName)
                .toUriString();
    }
}
