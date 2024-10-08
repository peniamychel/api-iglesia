package com.mcmm.model.dao;

import com.mcmm.model.entity.Iglesia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IglesiaDao extends JpaRepository<Iglesia, Long> {
}
