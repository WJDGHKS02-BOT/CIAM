package com.samsung.ciam.repositories;

import com.samsung.ciam.models.CisCountry;
import com.samsung.ciam.models.ConsentTypes;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ConsentTypesRepository extends JpaRepository<ConsentTypes, String> {

    @Override
    List<ConsentTypes> findAll();
}
