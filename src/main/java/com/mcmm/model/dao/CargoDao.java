package com.mcmm.model.dao;

import com.mcmm.model.entity.Cargo;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CargoDao extends JpaRepository<Cargo, Long> {

    @Modifying
    @Transactional
    @Query("UPDATE Cargo c SET c.estado = NOT c.estado, c.updatedAt = CURRENT_TIMESTAMP WHERE c.id = :id")
    void toggleEstado(Long id);

}
