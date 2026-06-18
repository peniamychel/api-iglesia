package com.mcmm.model.dao;

import com.mcmm.model.entity.Miembro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MiembroDao extends JpaRepository<Miembro, Long> {

    @Query("SELECT m FROM Miembro m WHERE m.ci = :ci")
    Miembro findByCi(@Param("ci") String ci);

    @Query("SELECT m FROM Miembro m WHERE m.estado = true AND m.id NOT IN (SELECT mi.miembro.id FROM MiembroIglesia mi WHERE mi.estado = true)")
    java.util.List<Miembro> findSinIglesia();
}
