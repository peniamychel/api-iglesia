package com.mcmm.model.dao;

import com.mcmm.model.entity.Ofrenda;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface OfrendaDao extends JpaRepository<Ofrenda, Long> {

    // Todas las consultas de lectura excluyen los movimientos borrados logicamente.
    // NULL-safe: las filas antiguas con estado = NULL se consideran activas.

    @Query("SELECT o FROM Ofrenda o WHERE (o.estado IS NULL OR o.estado = true)")
    List<Ofrenda> findAllActive();

    @Query("SELECT o FROM Ofrenda o WHERE o.iglesia.id = :iglesiaId AND (o.estado IS NULL OR o.estado = true)")
    List<Ofrenda> findByIglesiaId(@Param("iglesiaId") Long iglesiaId);

    @Query("SELECT o FROM Ofrenda o WHERE o.iglesia.id = :iglesiaId AND o.fechaRecaudacion BETWEEN :startDate AND :endDate AND (o.estado IS NULL OR o.estado = true)")
    List<Ofrenda> findByIglesiaIdAndFechaRecaudacionBetween(@Param("iglesiaId") Long iglesiaId, @Param("startDate") Date startDate, @Param("endDate") Date endDate);

    @Query("SELECT o FROM Ofrenda o WHERE o.fechaRecaudacion BETWEEN :startDate AND :endDate AND (o.estado IS NULL OR o.estado = true)")
    List<Ofrenda> findByFechaRecaudacionBetween(@Param("startDate") Date startDate, @Param("endDate") Date endDate);

    @Query("SELECT COALESCE(SUM(o.monto), 0.0) FROM Ofrenda o WHERE o.iglesia.id = :iglesiaId AND o.tipoMovimiento = :tipo AND o.fechaRecaudacion BETWEEN :start AND :end AND (o.estado IS NULL OR o.estado = true)")
    Double sumMontoByIglesiaAndTipoAndPeriod(@Param("iglesiaId") Long iglesiaId, @Param("tipo") String tipo, @Param("start") Date start, @Param("end") Date end);

    @Query("SELECT COALESCE(SUM(o.monto), 0.0) FROM Ofrenda o WHERE o.tipoMovimiento = :tipo AND o.fechaRecaudacion BETWEEN :start AND :end AND (o.estado IS NULL OR o.estado = true)")
    Double sumMontoByTipoAndPeriod(@Param("tipo") String tipo, @Param("start") Date start, @Param("end") Date end);
}
