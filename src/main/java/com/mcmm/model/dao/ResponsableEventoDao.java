package com.mcmm.model.dao;

import com.mcmm.model.entity.ResponsableEvento;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ResponsableEventoDao extends JpaRepository<ResponsableEvento, Long> {

    @Modifying
    @Transactional
    @Query("UPDATE ResponsableEvento r SET r.estado = NOT r.estado, r.updatedAt = CURRENT_TIMESTAMP WHERE r.id = :id")
    void toggleEstado(@Param("id") Long id);

    @Query("SELECT r FROM ResponsableEvento r WHERE r.evento.iglesia.id = :iglesiaId")
    java.util.List<ResponsableEvento> findByEventoIglesiaId(@Param("iglesiaId") Long iglesiaId);
}