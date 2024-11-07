package com.samsung.ciam.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 1. 파일명   : Subsidiary.java
 * 2. 패키지   : com.samsung.ciam.models
 * 3. 설명     : 법인 정보 테이블 (JPA) -> 실제 비즈니스 로직에 사용 X
 * 4. 작성자   : 서정환
 * 5. 작성일자 : 2024. 11. 04.
 * 6. 히스토리 :
 * <p>
 * -----------------------------------------------------------------
 * <p>
 * 날짜         | 이름         | 설명
 * <p>
 * -------------|--------------|------------------------------------
 * <p>
 * 2024. 11. 04 | 서정환       | 최초작성
 * <p>
 * -----------------------------------------------------------------
 */
@Entity
@Table(name = "subsidiaries")
public class Subsidiary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; //고유 식별자

    @Column(name = "company_code")
    private String companyCode; //회사 코드

    @Column(name = "subsidiary")
    private String subsidiary; //법인

    @Column(name = "country_key")
    private String countryKey; //국가 코드

    @Column(name = "name_en")
    private String nameEn; //법인 이름(영어)

    @Column(name = "name_ko")
    private String nameKo; //법인 이름(한국어)

    @Column(name = "created_at")
    private LocalDateTime createdAt; //등록일시

    @Column(name = "updated_at")
    private LocalDateTime updatedAt; //수정일시

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCompanyCode() {
        return companyCode;
    }

    public void setCompanyCode(String companyCode) {
        this.companyCode = companyCode;
    }

    public String getSubsidiary() {
        return subsidiary;
    }

    public void setSubsidiary(String subsidiary) {
        this.subsidiary = subsidiary;
    }

    public String getCountryKey() {
        return countryKey;
    }

    public void setCountryKey(String countryKey) {
        this.countryKey = countryKey;
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