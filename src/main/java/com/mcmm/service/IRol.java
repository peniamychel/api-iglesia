package com.mcmm.service;

import com.mcmm.model.entity.ERole;
import com.mcmm.model.entity.Privilegio;
import com.mcmm.model.entity.Rol;

import java.util.List;
import java.util.Set;

public interface IRol {

    Rol findOrCreateRol(ERole name);

    Set<Rol> getRolesByNames(Set<ERole> roleNames);

    List<Rol> findAll();

    Rol findById(Long id);

    Rol findByName(ERole name);

    Rol addPrivilegio(ERole roleName, Long privilegioId);

    Rol removePrivilegio(ERole roleName, Long privilegioId);

    Set<Privilegio> getPrivilegiosByRol(ERole roleName);
}
