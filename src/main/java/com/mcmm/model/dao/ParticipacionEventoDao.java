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

    @Modifying
    @Transactional
    @Query("UPDATE ParticipacionEvento p SET p.certificado = null, p.updatedAt = CURRENT_TIMESTAMP WHERE p.certificado.id = :certificadoId")
    void detachCertificado(@Param("certificadoId") Long certificadoId);

    @Query("SELECT p FROM ParticipacionEvento p " +
           "LEFT JOIN FETCH p.certificado " +
           "JOIN FETCH p.miembro " +
           "JOIN FETCH p.evento " +
           "LEFT JOIN FETCH p.entregadoPor " +
           "WHERE p.evento.iglesia.id = :iglesiaId " +
           "OR EXISTS (SELECT 1 FROM MiembroIglesia mi WHERE mi.miembro.id = p.miembro.id AND mi.iglesia.id = :iglesiaId AND mi.estado = true)")
    java.util.List<ParticipacionEvento> findByEventoIglesiaIdWithRelations(@Param("iglesiaId") Long iglesiaId);

    @Query("SELECT p FROM ParticipacionEvento p " +
           "LEFT JOIN FETCH p.certificado " +
           "JOIN FETCH p.miembro " +
           "JOIN FETCH p.evento " +
           "LEFT JOIN FETCH p.entregadoPor")
    java.util.List<ParticipacionEvento> findAllWithRelations();

    @Query("SELECT COUNT(p) > 0 FROM ParticipacionEvento p WHERE p.evento.id = :eventoId AND p.miembro.id = :miembroId")
    boolean existsByEventoIdAndMiembroId(@Param("eventoId") Long eventoId, @Param("miembroId") Long miembroId);

    /** True si el evento tiene al menos un participante/miembro registrado (guard de borrado). */
    boolean existsByEventoId(Long eventoId);

    @Query("SELECT p FROM ParticipacionEvento p " +
           "LEFT JOIN FETCH p.certificado c " +
           "JOIN FETCH p.miembro " +
           "JOIN FETCH p.evento e " +
           "JOIN FETCH e.iglesia " +
           "LEFT JOIN FETCH p.entregadoPor " +
           "WHERE p.codigoUnico = :codigoUnico")
    java.util.Optional<ParticipacionEvento> findByCodigoUnicoWithRelations(@Param("codigoUnico") String codigoUnico);
}