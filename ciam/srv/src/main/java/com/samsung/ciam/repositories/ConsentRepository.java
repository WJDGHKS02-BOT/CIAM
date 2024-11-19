package com.samsung.ciam.repositories;

import com.samsung.ciam.models.Consent;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ConsentRepository extends JpaRepository<Consent, Long> {

    List<Consent> findByGroupIdIsNotNull();

    List<Consent> findByGroupId(String groupId);

    @Query("SELECT c FROM Consent c WHERE c.typeId = :typeId")
    List<Consent> selectByTypeId(@Param("typeId") String typeId);

    @Query("SELECT c FROM Consent c WHERE c.cdcConsentId = :cdcConsentId")
    List<Consent> selectByCdcConsentId(@Param("cdcConsentId") String cdcConsentId);

    @Query("SELECT c FROM Consent c WHERE c.regionId = :regionId")
    List<Consent> selectByRegionId(@Param("regionId") String regionId);

    @Query("SELECT c FROM Consent c WHERE c.id IN :consentIds ORDER BY c.coverage ASC, c.typeId ASC")
    List<Consent> selectConsentList(@Param("consentIds") List<Long> consentIds);

    @Query(value = "SELECT * FROM consents WHERE coverage LIKE :coverage AND countries LIKE %:country% AND type_id LIKE '%terms%' LIMIT 1", nativeQuery = true)
    Consent selectLatestTermsByCoverageAndCountry(@Param("coverage") String coverage, @Param("country") String country);

    @Query(value = "SELECT * FROM consents WHERE coverage LIKE :coverage AND countries LIKE %:country% AND type_id LIKE '%terms%' AND subsidiary = :subsidiary LIMIT 1", nativeQuery = true)
    Consent selectLatestTermsByCoverageAndCountrySubsidiary(@Param("coverage") String coverage, @Param("country") String country, @Param("subsidiary") String subsidiary);

    @Query(value = "SELECT * FROM consents WHERE coverage LIKE :coverage AND countries LIKE %:country% AND type_id LIKE '%privacy%' AND subsidiary = :subsidiary LIMIT 1", nativeQuery = true)
    Consent selectLatestTermsByCoverageAndCountrySubsidiaryPrivacy(@Param("coverage") String coverage, @Param("country") String country, @Param("subsidiary") String subsidiary);

    @Query(value = "SELECT * FROM consents WHERE coverage = :coverage AND countries LIKE %:country% AND type_id = :typeId AND region_id = :regionId LIMIT 1", nativeQuery = true)
    Consent selectLatestByCoverageCountryTypeAndRegion(@Param("coverage") String coverage, @Param("country") String country, @Param("typeId") String typeId, @Param("regionId") String regionId);

    @Query(value = "SELECT * FROM consents WHERE coverage = :coverage AND countries LIKE %:country% AND type_id = :typeId AND region_id <> :regionId  LIMIT 1", nativeQuery = true)
    Consent selectLatestByCoverageCountryTypeAndNotRegion(@Param("coverage") String coverage, @Param("country") String country, @Param("typeId") String typeId, @Param("regionId") String regionId);

    @Query(value = "SELECT * FROM consents WHERE coverage LIKE :coverage AND countries LIKE %:country% AND type_id LIKE 'terms' LIMIT 1", nativeQuery = true)
    Consent selectByCoverageAndCountryAndType(@Param("coverage") String coverage, @Param("country") String country);

    @Query(value = "SELECT * FROM consents WHERE coverage LIKE :coverage AND countries LIKE %:country% AND type_id LIKE 'privacy' LIMIT 1", nativeQuery = true)
    Consent selectFirstByCoverageAndCountryAndPrivacy(@Param("coverage") String coverage, @Param("country") String country);

    @Query(value = "SELECT * FROM consents WHERE coverage LIKE :coverage AND countries LIKE %:country% AND type_id LIKE 'privacy' LIMIT 1", nativeQuery = true)
    Consent selectByCoverageAndCountryAndPrivacy(@Param("coverage") String coverage, @Param("country") String country);

    @Query(value = "SELECT * FROM consents WHERE coverage LIKE 'common' AND id = :marketingId AND type_id LIKE 'b2b' LIMIT 1", nativeQuery = true)
    Consent selectCommonMarketingKR(@Param("marketingId") Long  marketingId);

    @Query(value = "SELECT * FROM consents WHERE coverage LIKE 'common' AND id = :marketingId AND type_id LIKE 'b2b' LIMIT 1", nativeQuery = true)
    Consent selectCommonMarketingOther(@Param("marketingId") Long  marketingId);

    @Query(value = "SELECT * FROM consents WHERE coverage LIKE :coverage AND type_id LIKE 'privacy' LIMIT 1", nativeQuery = true)
    Consent selectFirstByCoverageAndType(@Param("coverage") String coverage);

    @Query(value = "SELECT * FROM consents WHERE coverage LIKE :coverage AND type_id LIKE :typeId LIMIT 1", nativeQuery = true)
    Consent selectFirstByCoverageAndType(@Param("coverage") String coverage, @Param("typeId") String typeId);

    @Query(value = "select distinct group_id from CONSENTS where group_id not in (select distinct a.group_id from consents a inner join consent_contents b on a.id = b.consent_id where a.group_id is not null)", nativeQuery = true)
    List<String> selectDistinctGroupId();
    
    @Modifying
    @Transactional
    @Query(value = 
        "INSERT INTO consents " +
        "(id, type_id, uid, created_at, updated_at, cdc_consent_id, region_id, coverage, name, countries, default_language, group_id, subsidiary) " +
        "VALUES " +
        "(nextval('consents_id_seq'::regclass), :type, :uid, NOW() AT TIME ZONE 'UTC' + INTERVAL '9 hours', null, LOWER(:cdc_consent_id), :region_id, :channel, :name, :countries, :language, :group_name, :subsidiary) ", 
        nativeQuery = true)
        void insertConsentsManagement(
            @Param("type") String type, 
            @Param("uid") String uid, 
            @Param("cdc_consent_id") String cdcConsentId,
            @Param("region_id") String regionId,
            @Param("channel") String channel,
            @Param("name") String name,
            @Param("countries") String countries,
            @Param("language") String language,
            @Param("group_name") String groupName,
            @Param("subsidiary") String subsidiary
    );
    
    @Query(value = "SELECT case when count(*)>0 then 'y' else 'n' end as yn FROM consents WHERE coverage = :coverage AND type_id = :typeId AND countries like '%'||:countries||'%' AND subsidiary = :subsidiary", nativeQuery = true)
    String duplicationConsentCheck(
        @Param("coverage") String coverage, 
        @Param("typeId") String type, 
        @Param("countries") String countries,
        @Param("subsidiary") String subsidiary
    );
    
    @Query(value = "SELECT group_id FROM consents WHERE id = :id", nativeQuery = true)
    String getGroupId(@Param("id") int id);
    
    @Query(value = "select distinct language_id from consents c inner join consent_contents cc on c.id = cc.consent_id where cc.id < 2000 and c.type_id = :typeId and c.coverage = :coverage and c.countries LIKE '%'||:countries||'%' and cc.status_id ='published' ORDER BY language_id", nativeQuery = true)
    List<String> getLanguageList(@Param("typeId") String typeId, @Param("coverage") String coverage, @Param("countries") String countries);

    @Query(value = "select count(*) from consents c inner join consent_contents cc on c.id = cc.consent_id where cc.id < 2000 and c.coverage = :coverage", nativeQuery = true)
    int getContentCntByChannel(@Param("coverage") String coverage);

    @Query(value = "SELECT coverage FROM consents WHERE id = :id LIMIT 1", nativeQuery = true)
    String selectCoverageById(@Param("id") Long id);

    @Query(value = "SELECT id FROM consent_contents WHERE consent_id = :consentId AND status_id = 'published' LIMIT 1", nativeQuery = true)
    Long selectPublishedConsentContentId(@Param("consentId") Long consentId);

    @Query("SELECT consentId FROM ConsentContent WHERE id = :consentContentId")
    Long selectConsentIdByConsentContentId(@Param("consentContentId") Long consentContentId);


    
}