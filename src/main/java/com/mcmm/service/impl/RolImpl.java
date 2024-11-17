package com.mcmm.service.impl;


import com.mcmm.model.dao.RolDao;
import com.mcmm.model.entity.ERole;
import com.mcmm.model.entity.Rol;
import com.mcmm.service.IRol;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class RolImpl implements IRol {

    @Autowired
    private RolDao rolDao;

    @Autowired
    private EntityManager entityManager;

    public Rol findOrCreateRol(ERole name) {
        return rolDao.findByName(name)
                .orElseGet(() -> {
                    // Verificar nuevamente dentro de una transacción para evitar condiciones de carrera
                    try {
                        return rolDao.findByName(name)
                                .orElseGet(() -> {
                                    Rol newRol = new Rol();
                                    newRol.setName(name);
                                    entityManager.flush(); // Forzar la sincronización con la base de datos
                                    return rolDao.save(newRol);
                                });
                    } catch (Exception e) {
                        // Si ocurre un error de duplicado, intentar recuperar el rol existente
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

//    @PostConstruct
//    public void init() {
//        // Inicializar roles al arrancar la aplicación
//        Arrays.stream(ERole.values())
//                .forEach(this::findOrCreateRol);
//    }

}
