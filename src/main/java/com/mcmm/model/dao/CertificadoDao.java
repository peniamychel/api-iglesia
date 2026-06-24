package com.mcmm.model.dao;

import com.mcmm.model.entity.Certificado;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CertificadoDao extends JpaRepository<Certificado, Long> {

    @Modifying
    @Transactional
    @Query("UPDATE Certificado c SET c.estado = NOT c.estado, c.updatedAt = CURRENT_TIMESTAMP WHERE c.id = :id")
    void toggleEstado(@Param("id") Long id);

    @Query("SELECT c FROM Certificado c WHERE c.evento.iglesia.id = :iglesiaId")
    java.util.List<Certificado> findByEventoIglesiaId(@Param("iglesiaId") Long iglesiaId);
}