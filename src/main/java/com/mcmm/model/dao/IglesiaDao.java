package com.mcmm.model.dao;

import com.mcmm.model.entity.Iglesia;
import com.mcmm.model.entity.Persona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface IglesiaDao extends JpaRepository<Iglesia, Long> {

    /*busca nombre de iglesia si ya existe*/
    @Query("SELECT i FROM Iglesia i WHERE i.nombre = :nameIglesia")
    Iglesia buscarPorNombreIglesia(@Param("nameIglesia") String nameIglesia);
}
