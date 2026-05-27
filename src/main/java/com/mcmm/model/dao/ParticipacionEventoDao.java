package com.mcmm.model.dao;

import com.mcmm.model.entity.ParticipacionEvento;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ParticipacionEventoDao extends JpaRepository<ParticipacionEvento, Long> {

    @Modifying
    @Transactional
    @Query("UPDATE ParticipacionEvento p SET p.estado = NOT p.estado, p.updatedAt = CURRENT_TIMESTAMP WHERE p.id = :id")
    void toggleEstado(@Param("id") Long id);
}