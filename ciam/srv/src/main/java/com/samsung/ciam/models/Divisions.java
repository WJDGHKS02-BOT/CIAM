package com.samsung.ciam.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "divisions")
public class Divisions {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "country_key")
    private String countryKey;

    @Column(name = "company_code")
    private String companyCode;

    @Column(name = "division")
    private String division;

    @Column(name = "subsidiary_code")
    private String subsidiaryCode;

    @Column(name = "name_en")
    private String nameEn;

    @Column(name = "name_ko")
    private String nameKo;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCountryKey() {
        return countryKey;
    }

    public void setCountryKey(String countryKey) {
        this.countryKey = countryKey;
    }

    public String getCompanyCode() {
        return companyCode;
    }

    public void setCompanyCode(String companyCode) {
        this.companyCode = companyCode;
    }

    public String getDivision() {
        return division;
    }

    public void setDivision(String division) {
        this.division = division;
    }

    public String getSubsidiaryCode() {
        return subsidiaryCode;
    }

    public void setSubsidiaryCode(String subsidiaryCode) {
        this.subsidiaryCode = subsidiaryCode;
    }

    public String getNameEn() {
        return nameEn;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }

    public String getNameKo() {
        return nameKo;
    }

    public void setNameKo(String nameKo) {
        this.nameKo = nameKo;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}