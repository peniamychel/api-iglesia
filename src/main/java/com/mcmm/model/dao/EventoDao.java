package com.mcmm.model.dao;

import com.mcmm.model.entity.Evento;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EventoDao extends JpaRepository<Evento, Long> {

    @Modifying
    @Transactional
    @Query("UPDATE Evento e SET e.estado = NOT e.estado, e.updatedAt = CURRENT_TIMESTAMP WHERE e.id = :id")
    void toggleEstado(@Param("id") Long id);
}