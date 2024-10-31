package com.samsung.ciam.repositories;

import com.samsung.ciam.models.Divisions;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DivisionRepository extends JpaRepository<Divisions, Long> {

    @Query(value = "SELECT * FROM divisions d ORDER BY d.name_en ASC", nativeQuery = true)
    List<Divisions> findAllByOrderByNameEnAsc();
}