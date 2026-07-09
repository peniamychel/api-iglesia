package com.mcmm.service.impl;

import com.mcmm.exception.NotFoundExceptionResource;
import com.mcmm.model.dao.RolCargoDao;
import com.mcmm.model.dao.AccionDao;
import com.mcmm.model.dto.AccionDto;
import com.mcmm.model.dto.RolCargoDto;
import com.mcmm.model.entity.RolCargo;
import com.mcmm.model.entity.Accion;
import com.mcmm.service.IRolCargo;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class RolCargoImpl implements IRolCargo {

    private final RolCargoDao rolCargoDao;
    private final AccionDao accionDao;
    private final ModelMapper modelMapper = new ModelMapper();

    public RolCargoImpl(RolCargoDao rolCargoDao, AccionDao accionDao) {
        this.rolCargoDao = rolCargoDao;
        this.accionDao = accionDao;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RolCargoDto> findAll() {
        return rolCargoDao.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RolCargoDto> findAllCargo() {
        return rolCargoDao.findByEstadoTrueAndNombreRolNot("ADMIN").stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public RolCargoDto findById(Long id) {
        RolCargo rc = rolCargoDao.findById(id)
                .orElseThrow(() -> new NotFoundExceptionResource("RolCargo", "id", id));
        return mapToDto(rc);
    }

    @Override
    public RolCargoDto create(RolCargoDto rolCargoDto) {
        RolCargo rc = modelMapper.map(rolCargoDto, RolCargo.class);
        RolCargo saved = rolCargoDao.save(rc);
        return mapToDto(saved);
    }

    @Override
    public RolCargoDto update(RolCargoDto rolCargoDto) {
        RolCargo rc = rolCargoDao.findById(rolCargoDto.getId())
                .orElseThrow(() -> new NotFoundExceptionResource("RolCargo", "id", rolCargoDto.getId()));
        
        rc.setNombre(rolCargoDto.getNombre());
        rc.setTipo(rolCargoDto.getTipo());
        rc.setNombreRol(rolCargoDto.getNombreRol());
        if (rolCargoDto.getEstado() != null) {
            rc.setEstado(rolCargoDto.getEstado());
        }

        RolCargo updated = rolCargoDao.save(rc);
        return mapToDto(updated);
    }

    @Override
    public void delete(Long id) {
        RolCargo rc = rolCargoDao.findById(id)
                .orElseThrow(() -> new NotFoundExceptionResource("RolCargo", "id", id));
        rolCargoDao.delete(rc);
    }

    @Override
    public void estado(Long id) {
        rolCargoDao.toggleEstado(id);
    }

    @Override
    public RolCargoDto addAccion(Long rolCargoId, Long accionId) {
        RolCargo rc = rolCargoDao.findById(rolCargoId)
                .orElseThrow(() -> new NotFoundExceptionResource("RolCargo", "id", rolCargoId));
        Accion a = accionDao.findById(accionId)
                .orElseThrow(() -> new NotFoundExceptionResource("Accion", "id", accionId));
        
        rc.getAcciones().add(a);
        RolCargo saved = rolCargoDao.save(rc);
        return mapToDto(saved);
    }

    @Override
    public RolCargoDto removeAccion(Long rolCargoId, Long accionId) {
        RolCargo rc = rolCargoDao.findById(rolCargoId)
                .orElseThrow(() -> new NotFoundExceptionResource("RolCargo", "id", rolCargoId));
        Accion a = accionDao.findById(accionId)
                .orElseThrow(() -> new NotFoundExceptionResource("Accion", "id", accionId));
        
        rc.getAcciones().remove(a);
        RolCargo saved = rolCargoDao.save(rc);
        return mapToDto(saved);
    }

    private RolCargoDto mapToDto(RolCargo rc) {
        RolCargoDto dto = modelMapper.map(rc, RolCargoDto.class);
        if (rc.getAcciones() != null) {
            dto.setAcciones(rc.getAcciones().stream().map(a -> {
                AccionDto aDto = modelMapper.map(a, AccionDto.class);
                aDto.setAuthorityCode(a.getAuthorityCode());
                if (a.getServicio() != null) {
                    aDto.setServicioId(a.getServicio().getId());
                    aDto.setServicioCodigo(a.getServicio().getCodigo());
                }
                return aDto;
            }).collect(Collectors.toSet()));
        }
        return dto;
    }
}
