package com.samsung.ciam.repositories;

import com.samsung.ciam.models.CisCountry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CisCountryRepository extends JpaRepository<CisCountry, Long> {

    @Query("SELECT c FROM CisCountry c ORDER BY c.nameEn ASC")
    List<CisCountry> findAllOrderedByNameEn();

    @Query("SELECT c FROM CisCountry c WHERE c.countryCode IN :countryCodes ORDER BY c.nameEn ASC")
    List<CisCountry> selectCountriesByCodes(@Param("countryCodes") List<String> countryCodes);
}
