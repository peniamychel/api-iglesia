package com.mcmm.model.dao;

import com.mcmm.model.entity.Miembro;
import com.mcmm.model.entity.MiembroIglesia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MiembroIglesiaDao extends JpaRepository<MiembroIglesia, Long> {

    // Iterable<MiembroIglesia> findMiembrosIglesia(@Param("id") Long id);

    // @Query("SELECT m FROM Miembro m JOIN m.miembroIglesia mi JOIN mi.iglesia i
    // WHERE i.id = :iglesiaId")
    // Iterable<Miembro> findMiembrosByIglesiaId(@Param("iglesiaId") Long
    // iglesiaId);

    // @Query("SELECT m FROM Miembro m MiembroIglesia mi, Iglesia i WHERE m.id =
    // mi.idIglesia ")
    // Iterable<Miembro> findMiembrosIglesia(@Param("iglesiaId") Long iglesiaId);

    // @Query("SELECT i FROM Iglesia i WHERE i.nombre = :nameIglesia")
    // Iglesia buscarPorNombreIglesia(@Param("nameIglesia") String nameIglesia);

    @Query(value = "SELECT m.* FROM miembro m, miembros_iglesia mi, iglesia i WHERE m.id = mi.miembro AND i.id = mi.iglesia AND i.id = :iglesiaId", nativeQuery = true)
    Iterable<Miembro> findMiembrosIglesia2(@Param("iglesiaId") Long iglesiaId);

    @Query("SELECT mi.miembro FROM MiembroIglesia mi WHERE mi.iglesia.id = :iglesiaId AND mi.estado = true")
    List<Miembro> findMiembrosIglesia(@Param("iglesiaId") Long iglesiaId);

    boolean findByMiembro(Long id);

    @Query("SELECT mi FROM MiembroIglesia mi WHERE mi.miembro.id = :miembroId AND mi.estado = true")
    java.util.Optional<MiembroIglesia> findActiveByMiembroId(@Param("miembroId") Long miembroId);

    @Query("SELECT mi FROM MiembroIglesia mi WHERE (mi.iglesia.id = :iglesiaId OR mi.iglesiaDestino.id = :iglesiaId) AND mi.estadoTraspaso = 'PENDIENTE'")
    java.util.List<MiembroIglesia> findPendingTransfersForChurch(@Param("iglesiaId") Long iglesiaId);

    @Query("SELECT mi FROM MiembroIglesia mi WHERE mi.estadoTraspaso = 'PENDIENTE'")
    java.util.List<MiembroIglesia> findAllPendingTransfers();

    @Query("SELECT mi FROM MiembroIglesia mi WHERE mi.miembro.id = :miembroId ORDER BY mi.fecha DESC")
    java.util.List<MiembroIglesia> findHistorialByMiembroId(@Param("miembroId") Long miembroId);

    // Iterable<MiembroIglesia> findByIdMiembro(Long id);

    // Método para llamar al procedimiento almacenado
    @Query(value = "CALL obtener_iglesias_con_mas_miembros(:limite)", nativeQuery = true)
    List<Object[]> obtenerIglesiasConMasMiembros(@Param("limite") Long limite);

}
