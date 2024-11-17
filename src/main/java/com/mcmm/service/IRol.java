package com.mcmm.service;

import com.mcmm.model.entity.ERole;
import com.mcmm.model.entity.Rol;

import java.util.Set;

public interface IRol {

    public Rol findOrCreateRol(ERole name);

    public Set<Rol> getRolesByNames(Set<ERole> roleNames);
}
