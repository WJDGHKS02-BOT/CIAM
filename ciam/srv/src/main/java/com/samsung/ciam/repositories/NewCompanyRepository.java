package com.samsung.ciam.repositories;

import com.samsung.ciam.models.NewCompany;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NewCompanyRepository extends JpaRepository<NewCompany, Long> {
    Optional<NewCompany> findFirstByBizRegNo1(String bizRegNo1);

    @Query(value = "SELECT * FROM new_companies WHERE bpid = :bpid LIMIT 1", nativeQuery = true)
    NewCompany selectByBpid(@Param("bpid") String bpid);

    @Query(value = "SELECT * FROM new_companies WHERE country = :country", nativeQuery = true)
    List<NewCompany> selectByCountry(@Param("country") String country);

    @Query(value = "SELECT * FROM new_companies WHERE vendorcode = :vendorcode", nativeQuery = true)
    List<NewCompany> selectByVendorCode(@Param("vendorcode") String vendorcode);

    Optional<NewCompany> findByBpid(String text);
}