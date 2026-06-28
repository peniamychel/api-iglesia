package com.mcmm.service.impl;

import com.mcmm.exception.NotFoundExceptionResource;
import com.mcmm.model.dao.RolCargoDao;
import com.mcmm.model.dao.PrivilegioDao;
import com.mcmm.model.dto.RolCargoDto;
import com.mcmm.model.entity.RolCargo;
import com.mcmm.model.entity.Privilegio;
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
    private final PrivilegioDao privilegioDao;
    private final ModelMapper modelMapper = new ModelMapper();

    public RolCargoImpl(RolCargoDao rolCargoDao, PrivilegioDao privilegioDao) {
        this.rolCargoDao = rolCargoDao;
        this.privilegioDao = privilegioDao;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RolCargoDto> findAll() {
        return rolCargoDao.findAll().stream()
                .map(rc -> modelMapper.map(rc, RolCargoDto.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RolCargoDto> findAllCargo() {
        return rolCargoDao.findByEstadoTrueAndNombreRolNot("ADMIN").stream()
                .map(rc -> modelMapper.map(rc, RolCargoDto.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public RolCargoDto findById(Long id) {
        RolCargo rc = rolCargoDao.findById(id)
                .orElseThrow(() -> new NotFoundExceptionResource("RolCargo", "id", id));
        return modelMapper.map(rc, RolCargoDto.class);
    }

    @Override
    public RolCargoDto create(RolCargoDto rolCargoDto) {
        RolCargo rc = modelMapper.map(rolCargoDto, RolCargo.class);
        RolCargo saved = rolCargoDao.save(rc);
        return modelMapper.map(saved, RolCargoDto.class);
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
        return modelMapper.map(updated, RolCargoDto.class);
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
    public RolCargoDto addPrivilegio(Long rolCargoId, Long privilegioId) {
        RolCargo rc = rolCargoDao.findById(rolCargoId)
                .orElseThrow(() -> new NotFoundExceptionResource("RolCargo", "id", rolCargoId));
        Privilegio p = privilegioDao.findById(privilegioId)
                .orElseThrow(() -> new NotFoundExceptionResource("Privilegio", "id", privilegioId));
        
        rc.getPrivilegios().add(p);
        RolCargo saved = rolCargoDao.save(rc);
        return modelMapper.map(saved, RolCargoDto.class);
    }

    @Override
    public RolCargoDto removePrivilegio(Long rolCargoId, Long privilegioId) {
        RolCargo rc = rolCargoDao.findById(rolCargoId)
                .orElseThrow(() -> new NotFoundExceptionResource("RolCargo", "id", rolCargoId));
        Privilegio p = privilegioDao.findById(privilegioId)
                .orElseThrow(() -> new NotFoundExceptionResource("Privilegio", "id", privilegioId));
        
        rc.getPrivilegios().remove(p);
        RolCargo saved = rolCargoDao.save(rc);
        return modelMapper.map(saved, RolCargoDto.class);
    }
}
