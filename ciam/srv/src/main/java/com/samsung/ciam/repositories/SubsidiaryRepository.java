package com.samsung.ciam.repositories;

import com.samsung.ciam.models.Subsidiary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SubsidiaryRepository extends JpaRepository<Subsidiary, Long> {

    List<Subsidiary> findAllSubsidiariesBy();

    @Query(value = "SELECT s.subsidiary FROM subsidiaries s WHERE s.country_Key LIKE :countryKey ORDER BY s.name_en ASC", nativeQuery = true)
    List<String> findByCountryKeyOrderByNameEnAsc(@Param("countryKey") String countryKey);

    @Query(value = "SELECT company_code FROM subsidiaries WHERE subsidiary = :subsidiary LIMIT 1", nativeQuery = true)
    String selectCompanyCode(@Param("subsidiary") String subsidiary);
}