package com.mcmm.model.dao;

import com.mcmm.model.entity.Iglesia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IglesiaDao extends JpaRepository<Iglesia, Long> {

    /* busca nombre de iglesia si ya existe */
    @Query("SELECT i FROM Iglesia i WHERE i.nombre = :nameIglesia")
    Iglesia buscarPorNombreIglesia(@Param("nameIglesia") String nameIglesia);

    @Query("SELECT i FROM Iglesia i WHERE i.nombre = :nameIglesia AND (i.id IS NULL OR NOT (i.id = :id))")
    Iglesia buscarPorNombreIglesiaExceptoId(Long id, String nameIglesia);

    List<Iglesia> findAllByOrderByCreatedAtDesc();
}