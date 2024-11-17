package com.mcmm.model.dao;

import com.mcmm.model.dto.GraficoDataDto;
import com.mcmm.model.entity.Iglesia;
import com.mcmm.model.entity.Miembro;
import com.mcmm.model.entity.MiembroIglesia;
import org.hibernate.annotations.NamedNativeQuery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MiembroIglesiaDao extends JpaRepository<MiembroIglesia, Long> {

//    Iterable<MiembroIglesia> findMiembrosIglesia(@Param("id") Long id);


//    @Query("SELECT m FROM Miembro m JOIN m.miembroIglesia mi JOIN mi.iglesia i WHERE i.id = :iglesiaId")
//    Iterable<Miembro> findMiembrosByIglesiaId(@Param("iglesiaId") Long iglesiaId);

//    @Query("SELECT m FROM Miembro m  MiembroIglesia mi, Iglesia i WHERE m.id = mi.idIglesia ")
//    Iterable<Miembro> findMiembrosIglesia(@Param("iglesiaId") Long iglesiaId);


//    @Query("SELECT i FROM Iglesia i WHERE i.nombre = :nameIglesia")
//    Iglesia buscarPorNombreIglesia(@Param("nameIglesia") String nameIglesia);

    @Query(value = "SELECT m.* FROM miembro m, miembros_iglesia mi, iglesia i WHERE m.id = mi.id_miembros AND i.id = mi.id_iglesia AND i.id = :iglesiaId", nativeQuery = true)
    Iterable<Miembro> findMiembrosIglesia2(@Param("iglesiaId") Long iglesiaId);


    @Query(value = "SELECT m.* " +
            "FROM miembro m " +
            "JOIN miembros_iglesia mi ON m.id = mi.id_miembros " +
            "JOIN iglesia i ON i.id = mi.id_iglesia " +
            "WHERE i.id = :iglesiaId", nativeQuery = true)
    Iterable<Miembro> findMiembrosIglesia(@Param("iglesiaId") Long iglesiaId);

    boolean findByIdMiembro(Long id);

//    Iterable<MiembroIglesia> findByIdMiembro(Long id);

    // Método para llamar al procedimiento almacenado
    @Query(value = "CALL ObtenerIglesiasConMasMiembros(:limite)", nativeQuery = true)
    List<Object[]> obtenerIglesiasConMasMiembros(@Param("limite") Long limite);


}
