package com.samsung.ciam.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 1. 파일명   : SfdcProduct.java
 * 2. 패키지   : com.samsung.ciam.models
 * 3. 설명     : SFCC 필드 관리 테이블 (JPA) -> 실제 비즈니스 로직에 사용 X
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
@Table(name = "sfdc_products")
public class SfdcProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; //고유 식별자

    @Column(name = "subsidiary")
    private String subsidiary; //법인코드

    @Column(name = "level")
    private String level; //레벨

    @Column(name = "product_category")
    private String productCategory; //카테고리

    @Column(name = "product_name")
    private String productName; //필드 이름

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

    public String getSubsidiary() {
        return subsidiary;
    }

    public void setSubsidiary(String subsidiary) {
        this.subsidiary = subsidiary;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getProductCategory() {
        return productCategory;
    }

    public void setProductCategory(String productCategory) {
        this.productCategory = productCategory;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
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