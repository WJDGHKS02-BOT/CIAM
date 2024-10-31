package com.samsung.ciam.repositories;

import com.samsung.ciam.models.AuthorizedSubsidiary;
import com.samsung.ciam.models.CisCountry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AuthorizedSubsidiaryRepository extends JpaRepository<AuthorizedSubsidiary, Long> {

    // search all
    public List<AuthorizedSubsidiary> findAll();

    @Query(value = "SELECT authorized_subsidiary_list FROM authorized_subsidiaries d WHERE channel = :channel AND cdc_uid = :uid LIMIT 1", nativeQuery = true)
    String selectSubsidList(@Param("channel") String channel,@Param("uid") String uid);

    @Query(value = "SELECT * FROM authorized_subsidiaries d WHERE channel = :channel AND cdc_uid = :uid AND authorized_subsidiary_list = :subsidiaries LIMIT 1", nativeQuery = true)
    AuthorizedSubsidiary selectAutoSubsidiary(@Param("channel") String channel,@Param("uid") String uid, @Param("subsidiaries") String subsidiaries);
}
