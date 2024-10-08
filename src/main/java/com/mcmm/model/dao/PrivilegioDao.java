package com.mcmm.model.dao;

import com.mcmm.model.entity.Privilegio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrivilegioDao extends JpaRepository<Privilegio, Long> {
    // Puedes agregar métodos de consulta personalizados aquí si es necesario
}