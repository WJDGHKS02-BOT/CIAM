package com.samsung.ciam.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.samsung.ciam.models.ConsentLanguage;

public interface ConsentLanguagesRepository extends JpaRepository<ConsentLanguage, String> {
    List<ConsentLanguage> findAll();
}
