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

    /**
     * Obtiene miembros activos que NO están asignados a ninguna iglesia activa
     * Y que NO tienen un cargo activo de tipo PASTOR.
     * El patrón se pasa como parámetro para evitar conflictos con el parser de formato de jboss-logging.
     */
    @Query("SELECT m FROM Miembro m WHERE m.estado = true " +
           "AND m.id NOT IN (SELECT mi.miembro.id FROM MiembroIglesia mi WHERE mi.estado = true) " +
           "AND m.id NOT IN (SELECT c.miembro.id FROM Cargo c WHERE c.estado = true AND UPPER(c.rolCargo.nombre) LIKE :patronPastor)")
    java.util.List<Miembro> findSinIglesiaParaAsignacion(@Param("patronPastor") String patronPastor);
    @Query("SELECT m FROM Miembro m LEFT JOIN MiembroIglesia mi ON mi.miembro.id = m.id AND mi.estado = true WHERE " +
           "(:searchText IS NULL OR :searchText = '' OR " +
           "LOWER(m.nombre) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
           "LOWER(m.apellido) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
           "LOWER(m.ci) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
           "LOWER(m.celular) LIKE LOWER(CONCAT('%', :searchText, '%')) OR " +
           "LOWER(m.direccion) LIKE LOWER(CONCAT('%', :searchText, '%'))) " +
           "AND (:estado IS NULL OR m.estado = :estado) " +
           "AND (:iglesiaNombre IS NULL OR :iglesiaNombre = 'all' OR mi.iglesia.nombre = :iglesiaNombre)")
    org.springframework.data.domain.Page<Miembro> searchMiembros(
            @Param("searchText") String searchText, 
            @Param("estado") Boolean estado, 
            @Param("iglesiaNombre") String iglesiaNombre, 
            org.springframework.data.domain.Pageable pageable);
}
