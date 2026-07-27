package com.mcmm.service.impl;

import com.mcmm.exception.NotFoundExceptionResource;
import com.mcmm.model.dao.CargoDao;
import com.mcmm.model.dao.RolCargoDao;
import com.mcmm.model.dao.IglesiaDao;
import com.mcmm.model.dao.MiembroDao;
import com.mcmm.model.dto.CargoDto;
import com.mcmm.model.entity.Cargo;
import com.mcmm.model.entity.RolCargo;
import com.mcmm.model.entity.Iglesia;
import com.mcmm.model.entity.Miembro;
import com.mcmm.service.FileStorageService;
import com.mcmm.service.ICargo;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CargoImpl implements ICargo {

    private static final String CARGOS_DIR = "cargos/";

    private final ModelMapper modelMapper;
    private final CargoDao cargoDao;
    private final IglesiaDao iglesiaDao;
    private final MiembroDao miembroDao;
    private final RolCargoDao rolCargoDao;
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
    public Iterable<CargoDto> findAll() {
        List<CargoDto> cargosDtos = new ArrayList<>();
        Long iglesiaId = getCurrentIglesiaId();
        Iterable<Cargo> cargos = iglesiaId != null ? cargoDao.findByIglesiaId(iglesiaId) : cargoDao.findAll();

        for (Cargo cargo : cargos) {
            CargoDto cargoDto = modelMapper.map(cargo, CargoDto.class);
            cargosDtos.add(cargoDto);
        }
        return cargosDtos;
    }

    @Override
    @Transactional(readOnly = true)
    public CargoDto findById(Long id) {
        Cargo cargo = cargoDao.findById(id)
                .orElseThrow(() -> new NotFoundExceptionResource("Cargo", "id", id));
        return modelMapper.map(cargo, CargoDto.class);
    }

    @Override
    @Transactional
    public CargoDto create(CargoDto cargoDto) {
        Cargo cargo = modelMapper.map(cargoDto, Cargo.class);

        //tratar id de iglesia
        if (cargoDto.getIglesiaId() != null) {
            Iglesia iglesia = iglesiaDao.findById(cargoDto.getIglesiaId())
                    .orElseThrow(() -> new NotFoundExceptionResource("Iglesia", "id", cargoDto.getIglesiaId()));
            if (!iglesia.getEstado()) throw new IllegalArgumentException("La iglesia proporcionada está inactiva.");
            cargo.setIglesia(iglesia);
        } else {
            Long iglesiaId = getCurrentIglesiaId();
            if (iglesiaId != null) {
                Iglesia iglesia = iglesiaDao.findById(iglesiaId)
                        .orElseThrow(() -> new NotFoundExceptionResource("Iglesia", "id", iglesiaId));
                cargo.setIglesia(iglesia);
            }
        }

        //tratar id de miembro
        if (cargoDto.getIdMiembro() != null) {
            if (cargoDao.existsByMiembroIdAndEstadoTrue(cargoDto.getIdMiembro())) {
                throw new IllegalArgumentException("Este miembro ya tiene un rol activo.");
            }
            Miembro miembro = miembroDao.findById(cargoDto.getIdMiembro())
                    .orElseThrow(() -> new NotFoundExceptionResource("Miembro", "id", cargoDto.getIdMiembro()));
            if (!miembro.getEstado()) throw new IllegalArgumentException("El miembro proporcionado está inactivo.");
            cargo.setMiembro(miembro);
        }

        //tratar id rol cargo
        if (cargoDto.getRolCargoId() != null) {
            RolCargo rolCargo = rolCargoDao.findById(cargoDto.getRolCargoId())
                    .orElseThrow(() -> new NotFoundExceptionResource("RolCargo", "id", cargoDto.getRolCargoId()));
            if (!rolCargo.getEstado()) throw new IllegalArgumentException("El Rol/Cargo proporcionado está inactivo.");
            cargo.setRolCargo(rolCargo);
        }

        Cargo savedCargo = cargoDao.save(cargo);
        return modelMapper.map(savedCargo, CargoDto.class);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!cargoDao.existsById(id)) {
            throw new NotFoundExceptionResource("Cargo", "id", id);
        }
        cargoDao.deleteById(id);
    }

    @Override
    @Transactional
    public void estado(Long id) {
        if (!cargoDao.existsById(id)) {
            throw new NotFoundExceptionResource("Cargo", "id", id);
        }
        cargoDao.toggleEstado(id);
    }

    @Override
    @Transactional
    public CargoDto update(CargoDto cargoDto) {
        Cargo cargoR = cargoDao.findById(cargoDto.getId())
                .orElseThrow(() -> new NotFoundExceptionResource("Cargo", "id", cargoDto.getId()));

        if (cargoDto.getIglesiaId() != null) {
            Iglesia iglesia = iglesiaDao.findById(cargoDto.getIglesiaId())
                    .orElseThrow(() -> new NotFoundExceptionResource("Iglesia", "id", cargoDto.getIglesiaId()));
            if (!iglesia.getEstado()) throw new IllegalArgumentException("La iglesia proporcionada está inactiva.");
            cargoR.setIglesia(iglesia);
        } else {
            Long iglesiaId = getCurrentIglesiaId();
            if (iglesiaId != null) {
                Iglesia iglesia = iglesiaDao.findById(iglesiaId)
                        .orElseThrow(() -> new NotFoundExceptionResource("Iglesia", "id", iglesiaId));
                cargoR.setIglesia(iglesia);
            }
        }

        if (cargoDto.getIdMiembro() != null) {
            Miembro miembro = miembroDao.findById(cargoDto.getIdMiembro())
                    .orElseThrow(() -> new NotFoundExceptionResource("Miembro", "id", cargoDto.getIdMiembro()));
            if (!miembro.getEstado()) throw new IllegalArgumentException("El miembro proporcionado está inactivo.");
            cargoR.setMiembro(miembro);
        }

        if (cargoDto.getRolCargoId() != null) {
            RolCargo rolCargo = rolCargoDao.findById(cargoDto.getRolCargoId())
                    .orElseThrow(() -> new NotFoundExceptionResource("RolCargo", "id", cargoDto.getRolCargoId()));
            if (!rolCargo.getEstado()) throw new IllegalArgumentException("El Rol/Cargo proporcionado está inactivo.");
            cargoR.setRolCargo(rolCargo);
        }

        cargoR.setDetalle(cargoDto.getDetalle());
        cargoR.setFechaInicio(cargoDto.getFechaInicio());
        cargoR.setFechaFin(cargoDto.getFechaFin());

        Cargo updatedCargo = cargoDao.save(cargoR);
        return modelMapper.map(updatedCargo, CargoDto.class);
    }

    @Override
    @Transactional
    public CargoDto save(CargoDto cargoDto) {
        try {
            Cargo cargo = modelMapper.map(cargoDto, Cargo.class);
            Cargo saveCargo = cargoDao.save(cargo);
            return modelMapper.map(saveCargo, CargoDto.class);
        } catch (DataIntegrityViolationException ex) {
            throw new IllegalArgumentException("Error de integridad de datos en la base de datos.");
        }
    }

    @Override
    @Transactional
    public String saveActaAsignacion(Long id, MultipartFile file) throws IOException {
        Cargo cargo = cargoDao.findById(id)
                .orElseThrow(() -> new NotFoundExceptionResource("Cargo", "id", id));

        if (cargo.getUriActaAsignacion() != null && !cargo.getUriActaAsignacion().isBlank()) {
            fileStorageService.deleteFile(CARGOS_DIR + cargo.getUriActaAsignacion());
        }

        String fileName = fileStorageService.storeFile(file, "acta-asignacion-cargo-" + id, CARGOS_DIR);
        cargo.setUriActaAsignacion(fileName);
        cargoDao.save(cargo);

        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(uploadDir).path("/").path(CARGOS_DIR).path(fileName).toUriString();
    }

    @Override
    @Transactional
    public String saveActaDeslindacion(Long id, MultipartFile file) throws IOException {
        Cargo cargo = cargoDao.findById(id)
                .orElseThrow(() -> new NotFoundExceptionResource("Cargo", "id", id));

        if (cargo.getUriActaDeslindacion() != null && !cargo.getUriActaDeslindacion().isBlank()) {
            fileStorageService.deleteFile(CARGOS_DIR + cargo.getUriActaDeslindacion());
        }

        String fileName = fileStorageService.storeFile(file, "acta-deslindacion-cargo-" + id, CARGOS_DIR);
        cargo.setUriActaDeslindacion(fileName);
        cargoDao.save(cargo);

        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path(uploadDir).path("/").path(CARGOS_DIR).path(fileName).toUriString();
    }

    @Override
    @Transactional
    public boolean estadoConFecha(Long id, Date fechaFin) {
        Cargo cargo = cargoDao.findById(id)
                .orElseThrow(() -> new NotFoundExceptionResource("Cargo", "id", id));

        boolean nuevoEstado = !cargo.getEstado();
        cargo.setEstado(nuevoEstado);

        if (!nuevoEstado) {
            cargo.setFechaFin(fechaFin != null ? fechaFin : new Date());
        } else {
            cargo.setFechaFin(null);
            if (cargo.getUriActaDeslindacion() != null && !cargo.getUriActaDeslindacion().isBlank()) {
                try {
                    fileStorageService.deleteFile(CARGOS_DIR + cargo.getUriActaDeslindacion());
                } catch (IOException ignored) {}
            }
            cargo.setUriActaDeslindacion(null);
        }

        cargoDao.save(cargo);
        return nuevoEstado;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CargoDto> findMisColaboradores() {
        Long iglesiaId = getCurrentIglesiaId();
        List<Cargo> cargos;
        if (iglesiaId != null) {
            cargos = cargoDao.findByIglesiaId(iglesiaId);
        } else {
            cargos = cargoDao.findAll();
        }

        List<CargoDto> result = new ArrayList<>();
        for (Cargo cargo : cargos) {
            CargoDto dto = new CargoDto();
            dto.setId(cargo.getId());
            dto.setIglesiaId(cargo.getIglesia() != null ? cargo.getIglesia().getId() : null);
            dto.setRolCargoId(cargo.getRolCargo() != null ? cargo.getRolCargo().getId() : null);
            dto.setIdMiembro(cargo.getMiembro() != null ? cargo.getMiembro().getId() : null);
            dto.setDetalle(cargo.getDetalle());
            dto.setFechaInicio(cargo.getFechaInicio());
            dto.setFechaFin(cargo.getFechaFin());
            dto.setEstado(cargo.getEstado());
            dto.setUriActaAsignacion(cargo.getUriActaAsignacion());
            dto.setUriActaDeslindacion(cargo.getUriActaDeslindacion());
            dto.setCreatedAt(cargo.getCreatedAt());
            dto.setUpdatedAt(cargo.getUpdatedAt());

            // Embed miembro info
            if (cargo.getMiembro() != null) {
                com.mcmm.model.entity.Miembro m = cargo.getMiembro();
                String fotoUrl = null;
                if (m.getUriFoto() != null && !m.getUriFoto().isBlank()) {
                    fotoUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                            .path(uploadDir).path("/").path("miembros/").path(m.getUriFoto()).toUriString();
                }
                dto.setMiembro(CargoDto.MiembroInfo.builder()
                        .id(m.getId())
                        .nombre(m.getNombre())
                        .apellido(m.getApellido())
                        .ci(m.getCi())
                        .uriFoto(fotoUrl)
                        .build());
            }

            // Embed rolCargo info
            if (cargo.getRolCargo() != null) {
                com.mcmm.model.entity.RolCargo rc = cargo.getRolCargo();
                dto.setRolCargo(CargoDto.RolCargoInfo.builder()
                        .id(rc.getId())
                        .nombre(rc.getNombre())
                        .tipo(rc.getTipo())
                        .build());
            }

            result.add(dto);
        }
        return result;
    }
}
