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

    List<Ofrenda> findByIglesiaId(Long iglesiaId);

    List<Ofrenda> findByIglesiaIdAndFechaRecaudacionBetween(Long iglesiaId, Date startDate, Date endDate);

    List<Ofrenda> findByFechaRecaudacionBetween(Date startDate, Date endDate);

    @Query("SELECT COALESCE(SUM(o.monto), 0.0) FROM Ofrenda o WHERE o.iglesia.id = :iglesiaId AND o.tipoMovimiento = :tipo AND o.fechaRecaudacion BETWEEN :start AND :end")
    Double sumMontoByIglesiaAndTipoAndPeriod(@Param("iglesiaId") Long iglesiaId, @Param("tipo") String tipo, @Param("start") Date start, @Param("end") Date end);

    @Query("SELECT COALESCE(SUM(o.monto), 0.0) FROM Ofrenda o WHERE o.tipoMovimiento = :tipo AND o.fechaRecaudacion BETWEEN :start AND :end")
    Double sumMontoByTipoAndPeriod(@Param("tipo") String tipo, @Param("start") Date start, @Param("end") Date end);
}
