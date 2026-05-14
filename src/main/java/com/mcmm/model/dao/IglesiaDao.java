package com.mcmm.model.dao;

import com.mcmm.model.entity.Iglesia;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IglesiaDao extends JpaRepository<Iglesia, Long> {

    /**
     * Busca una iglesia por nombre.
     * 
     * @param nameIglesia Nombre de la iglesia a buscar.
     * @return Iglesia encontrada.
     */
    @Query("SELECT i FROM Iglesia i WHERE i.nombre = :nameIglesia")
    Iglesia buscarPorNombreIglesia(@Param("nameIglesia") String nameIglesia);

    /**
     * Busca una iglesia por nombre y ID, excluyendo la iglesia con el ID
     * proporcionado.
     * 
     * @param id          ID de la iglesia a excluir.
     * @param nameIglesia Nombre de la iglesia a buscar.
     * @return Iglesia encontrada.
     */
    @Query("SELECT i FROM Iglesia i WHERE i.nombre = :nameIglesia AND (i.id IS NULL OR NOT (i.id = :id))")
    Iglesia buscarPorNombreIglesiaExceptoId(Long id, String nameIglesia);

    /**
     * Busca una iglesia por nombre y ID, excluyendo la iglesia con el ID
     * proporcionado.
     * 
     * @param nameIglesia Nombre de la iglesia a buscar.
     * @param id          ID de la iglesia a excluir.
     * @return Iglesia encontrada.
     */
    Iglesia findByNombreAndIdNot(String nameIglesia, Long id);

    /**
     * Busca todas las iglesias ordenadas por fecha de creación descendente.
     * 
     * @return Lista de iglesias ordenadas por fecha de creación descendente.
     */
    List<Iglesia> findAllByOrderByCreatedAtDesc();

    /**
     * Busca todas las iglesias activas con todos sus datos.
     * 
     * @return Lista de iglesias activas con todos sus datos.
     */
    @EntityGraph(attributePaths = { "cargos", "cargos.tipoCargo", "cargos.miembro", "cargos.miembro.persona" })
    List<Iglesia> findByEstadoTrue();

}