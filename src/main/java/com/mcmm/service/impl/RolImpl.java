package com.mcmm.service.impl;

import com.mcmm.model.dao.PrivilegioDao;
import com.mcmm.model.dao.RolDao;
import com.mcmm.model.entity.ERole;
import com.mcmm.model.entity.Privilegio;
import com.mcmm.model.entity.Rol;
import com.mcmm.service.IRol;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class RolImpl implements IRol {

    @Autowired
    private RolDao rolDao;

    @Autowired
    private PrivilegioDao privilegioDao;

    @Autowired
    private EntityManager entityManager;

    public Rol findOrCreateRol(ERole name) {
        return rolDao.findByName(name)
                .orElseGet(() -> {
                    try {
                        return rolDao.findByName(name)
                                .orElseGet(() -> {
                                    Rol newRol = new Rol();
                                    newRol.setName(name);
                                    entityManager.flush();
                                    return rolDao.save(newRol);
                                });
                    } catch (Exception e) {
                        return rolDao.findByName(name)
                                .orElseThrow(() -> new RuntimeException("Error al crear/recuperar rol: " + name));
                    }
                });
    }

    public Set<Rol> getRolesByNames(Set<ERole> roleNames) {
        return roleNames.stream()
                .map(this::findOrCreateRol)
                .collect(Collectors.toSet());
    }

    @Override
    public List<Rol> findAll() {
        return rolDao.findAll();
    }

    @Override
    public Rol findById(Long id) {
        return rolDao.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Rol no encontrado con ID: " + id));
    }

    @Override
    public Rol findByName(ERole name) {
        return rolDao.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException("Rol no encontrado: " + name));
    }

    @Override
    public Rol addPrivilegio(ERole roleName, Long privilegioId) {
        Rol rol = findByName(roleName);
        Privilegio privilegio = privilegioDao.findById(privilegioId)
                .orElseThrow(() -> new EntityNotFoundException("Privilegio no encontrado con ID: " + privilegioId));

        rol.getPrivilegios().add(privilegio);
        return rolDao.save(rol);
    }

    @Override
    public Rol removePrivilegio(ERole roleName, Long privilegioId) {
        Rol rol = findByName(roleName);
        Privilegio privilegio = privilegioDao.findById(privilegioId)
                .orElseThrow(() -> new EntityNotFoundException("Privilegio no encontrado con ID: " + privilegioId));

        rol.getPrivilegios().remove(privilegio);
        return rolDao.save(rol);
    }

    @Override
    public Set<Privilegio> getPrivilegiosByRol(ERole roleName) {
        Rol rol = findByName(roleName);
        return rol.getPrivilegios();
    }
}
