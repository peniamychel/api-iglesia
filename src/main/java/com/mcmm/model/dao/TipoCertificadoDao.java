package com.mcmm.model.dao;

import com.mcmm.model.entity.TipoCertificado;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TipoCertificadoDao extends JpaRepository<TipoCertificado, Long> {

    @Modifying
    @Transactional
    @Query("UPDATE TipoCertificado t SET t.estado = NOT t.estado, t.updatedAt = CURRENT_TIMESTAMP WHERE t.id = :id")
    void toggleEstado(@Param("id") Long id);
}