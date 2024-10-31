package com.samsung.ciam.repositories;

import com.samsung.ciam.models.SfdcProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SfdcProductRepository extends JpaRepository<SfdcProduct, Long> {

    @Query(value = "SELECT * FROM sfdc_products WHERE subsidiary LIKE :subsidiary AND level = :level ORDER BY product_category ASC", nativeQuery = true)
    List<SfdcProduct> findBySubsidiaryAndLevel(@Param("subsidiary") String subsidiary, @Param("level") String level);

    @Query(value = "SELECT * FROM sfdc_products WHERE subsidiary LIKE 'SEACE' AND level = 1 ORDER BY product_category ASC", nativeQuery = true)
    List<SfdcProduct> findBySubsidiarySeace();

    @Query(value = "SELECT * FROM sfdc_products WHERE subsidiary LIKE :subsidiary AND level = 2 AND product_category LIKE :productCategory ORDER BY product_name ASC", nativeQuery = true)
    List<SfdcProduct> findProductsBySubsidiaryAndProductCategory(@Param("subsidiary") String subsidiary, @Param("productCategory") String productCategory);
}