package com.samsung.ciam.repositories;

import com.samsung.ciam.models.NewSubsidiary;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NewSubsidiaryRepository  extends JpaRepository<NewSubsidiary, Long>{
    @Query(value = "SELECT company_code FROM new_subsidiary WHERE company_abbreviation = :subsidiary LIMIT 1", nativeQuery = true)
    String selectCompanyCode(@Param("subsidiary") String subsidiary);

    @Query(value = "SELECT company_abbreviation FROM new_subsidiary ORDER BY id ASC", nativeQuery = true)
    List<String> findByCompanyAbbreviationOrderByIdAsc();
}
