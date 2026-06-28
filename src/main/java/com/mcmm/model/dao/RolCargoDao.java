package com.mcmm.model.dao;

import com.mcmm.model.entity.RolCargo;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RolCargoDao extends JpaRepository<RolCargo, Long> {

    Optional<RolCargo> findByNombre(String nombre);
    Optional<RolCargo> findByNombreRol(String nombreRol);

    List<RolCargo> findByEstadoTrueAndNombreRolNot(String nombreRol);

    @Modifying
    @Transactional
    @Query("UPDATE RolCargo c SET c.estado = NOT c.estado, c.updatedAt = CURRENT_TIMESTAMP WHERE c.id = :id")
    void toggleEstado(@Param("id") Long id);
}
