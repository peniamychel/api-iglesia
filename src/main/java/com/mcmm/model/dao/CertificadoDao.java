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

    /** True si el evento ya tiene un certificado asociado (guard de borrado / casilla de edición). */
    boolean existsByEventoId(Long eventoId);

    /** IDs (distintos) de eventos que tienen certificado, dentro de un conjunto dado (evita N+1). */
    @Query("SELECT DISTINCT c.evento.id FROM Certificado c WHERE c.evento.id IN :ids")
    java.util.List<Long> findEventoIdsConCertificado(@Param("ids") java.util.List<Long> ids);
}