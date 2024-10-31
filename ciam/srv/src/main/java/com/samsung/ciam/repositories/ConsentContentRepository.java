package com.samsung.ciam.repositories;

import com.samsung.ciam.models.ConsentContent;
import com.samsung.ciam.models.UserAgreedConsents;

import jakarta.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.util.List;

public interface ConsentContentRepository extends JpaRepository<ConsentContent, Long> {

    @Query("SELECT c FROM ConsentContent c WHERE c.consentId = :consentId")
    List<ConsentContent> selectByConsentId(@Param("consentId") Long consentId);

    @Query("SELECT c FROM ConsentContent c WHERE c.languageId = :languageId")
    List<ConsentContent> selectByLanguageId(@Param("languageId") String languageId);

    @Query("SELECT c FROM ConsentContent c WHERE c.statusId = :statusId")
    List<ConsentContent> selectByStatusId(@Param("statusId") String statusId);

    @Query(value = "SELECT DISTINCT version FROM consent_contents ORDER BY version", nativeQuery = true)
    List<Integer> selectDistinctVersion();

    @Query(value = "SELECT * FROM consent_contents WHERE consent_id = :consentId AND language_id LIKE %:languageId% AND status_id LIKE %:statusId% ORDER BY version DESC LIMIT 1", nativeQuery = true)
    ConsentContent selectLatestByConsentIdAndLanguageIdAndStatusId(@Param("consentId") Long consentId, @Param("languageId") String languageId, @Param("statusId") String statusId);

    @Query(value = "SELECT * FROM consent_contents WHERE consent_id = :consentId  AND status_id LIKE %:statusId%  LIMIT 1", nativeQuery = true)
    ConsentContent selectLatestByConsentIdAndIdAndStatusId(@Param("consentId") Long consentId, @Param("statusId") String statusId);

    @Query(value = "SELECT * FROM consent_contents WHERE consent_id = :consentId ORDER BY version DESC LIMIT 1", nativeQuery = true)
    ConsentContent selectConsent(@Param("consentId") Long consentId);

    @Query(value = "SELECT * FROM consent_contents WHERE consent_id = :consentId AND language_id LIKE %:languageId% AND status_id LIKE 'published' ORDER BY version DESC LIMIT 1", nativeQuery = true)
    ConsentContent selectLatestByConsentIdAndLanguageId(@Param("consentId") Long consentId, @Param("languageId") String languageId);

    @Query(value = "SELECT * FROM consent_contents WHERE consent_id = :consentId AND language_id LIKE %:languageId% AND status_id LIKE 'published' ORDER BY version DESC LIMIT 1", nativeQuery = true)
    ConsentContent selectLatestByConsentIdAndLanguagePublishedId(@Param("consentId") Long consentId, @Param("languageId") String languageId);

    @Query(value = "SELECT * FROM consent_contents WHERE consent_id = :consentId AND status_id LIKE 'published' LIMIT 1", nativeQuery = true)
    ConsentContent selectLatestByConsentIdPublishedId(@Param("consentId") Long consentId);

    @Query(value = "SELECT * FROM consent_contents WHERE consent_id = :consentId AND status_id LIKE %:statusId% ORDER BY version DESC LIMIT 1", nativeQuery = true)
    ConsentContent selectConsent(@Param("consentId") Long consentId,@Param("statusId") String statusId);
    
    @Modifying
    @Transactional
    @Query(value = 
        "/* ConsentContentRepository.insertConsentContentManagement */ INSERT INTO consent_contents " +
        "(id, consent_id, language_id, version, status_id, purpose, content, uid, created_at, updated_at, released_at) " +
        "VALUES(nextval('consent_contents_id_seq'::regclass), :consentId, :language, :version, :status, :purpose, :content, :uid, NOW() AT TIME ZONE 'UTC' + INTERVAL '9 hours', null, :releasedAt) ", 
        nativeQuery = true)
        void insertConsentContentManagement(
            @Param("language") String language, 
            @Param("version") float version,
            @Param("status") String status, 
            @Param("purpose") String purpose,
            @Param("content") String content,
            @Param("uid") String uid,
            @Param("consentId") int consentId,
            @Param("releasedAt") Timestamp releasedAt
    );
    
    @Modifying
    @Transactional
    @Query(value = 
        "/* ConsentContentRepository.insertConsentContentManagement ( groupId is not null ) */ insert into consent_contents (id, consent_id, language_id, version, status_id, purpose, content, uid, created_at, released_at) " + 
        " select nextval('consent_contents_id_seq'::regclass) as id, id as consent_id, default_language as language_id, :version as version, :status as status_id, :purpose as purpose, :content as content, :uid as uid, (NOW() AT TIME ZONE 'UTC' + INTERVAL '9 hours') as created_at, :releasedAt as released_at from consents  " +
        " where group_id = :groupId ", nativeQuery = true)
        void insertConsentContentManagement(
            @Param("version") float version,
            @Param("status") String status, 
            @Param("purpose") String purpose,
            @Param("content") String content,
            @Param("uid") String uid,
            @Param("groupId") String groupId,
            @Param("releasedAt") Timestamp releasedAt
    );
    
    @Modifying
    @Transactional
    @Query(value = 
        "/* ConsentContentRepository.updateConsentContentStatusIdById */ UPDATE consent_contents SET status_id = :statusId, updated_at = (NOW() AT TIME ZONE 'UTC' + INTERVAL '9 hours'), content = :content, version = :version, released_at = :releasedAt where id = :id", 
        nativeQuery = true)
        void updateConsentContentStatusIdById(@Param("id") int id, @Param("statusId") String statusId, @Param("content") String content, @Param("version") float version, @Param("releasedAt") Timestamp releasedAt);
    
    // @Modifying
    // @Transactional
    // @Query(value = 
    //     "/* ConsentContentRepository.updateConsentContentStatusId */ UPDATE consent_contents SET status_id = :statusId, updated_at = (NOW() AT TIME ZONE 'UTC' + INTERVAL '9 hours') where consent_id = :consentId", 
    //     nativeQuery = true)
    //     void updateConsentContentStatusId(@Param("consentId") int consentId, @Param("statusId") String statusId);
    
    @Modifying
    @Transactional
    @Query(value = 
        "/* ConsentContentRepository.updateConsentContentStatusIdByGroupId */ UPDATE consent_contents SET status_id = :statusId, updated_at = (NOW() AT TIME ZONE 'UTC' + INTERVAL '9 hours'), content = :content, version = :version, released_at = :releasedAt where consent_id in (select id as consent_id from consents where group_id = :groupId and version = :currentVersion)", 
        nativeQuery = true)
        void updateConsentContentStatusIdByGroupId(@Param("groupId") String groupId, @Param("statusId") String statusId, @Param("content") String content, @Param("version") float version, @Param("releasedAt") Timestamp releasedAt, @Param("currentVersion") float currentVersion);
            
    @Query(value = "SELECT case when count(*)>0 then 'y' else 'n' end as yn FROM consent_contents WHERE consent_id in (select id from consents where coverage = :coverage AND type_id = :typeId AND countries like '%'||:countries||'%' AND subsidiary = :subsidiary)", nativeQuery = true)
    String duplicationConsentContentCheck(
        @Param("coverage") String coverage, 
        @Param("typeId") String type, 
        @Param("countries") String countries,
        @Param("subsidiary") String subsidiary
    );

    @Query(value = "SELECT case when count(*)>0 then 'y' else 'n' end as yn FROM consent_contents WHERE consent_id in (select id from consents where group_id = :consentGroup)", nativeQuery = true)
    String duplicationConsentContentCheck(
        @Param("consentGroup") String consentGroup
    );
    
    @Modifying
    @Transactional
    @Query(value = 
        "/* ConsentContentRepository.updateConsentContentStatusIdHistoric */ " +
        "UPDATE consent_contents SET status_id = 'historic', updated_at = (NOW() AT TIME ZONE 'UTC' + INTERVAL '9 hours') WHERE consent_id = :consentId and status_id = 'published'", nativeQuery = true)
        int updateConsentContentStatusIdHistoric(@Param("consentId") Long consentId);

    @Modifying
    @Transactional
    @Query(value = 
        "/* ConsentContentRepository.updateConsentContentStatusIdPublished */ " +
        "UPDATE consent_contents SET status_id = 'published', updated_at = (NOW() AT TIME ZONE 'UTC' + INTERVAL '9 hours') WHERE id = :id", nativeQuery = true)
        int updateConsentContentStatusIdPublished(@Param("id") Long id);

    @Query(value = "SELECT * FROM consent_contents WHERE released_at::DATE = CURRENT_DATE and status_id = 'scheduled'", nativeQuery = true)
    List<ConsentContent> listConsentContentStatusIdPublished();

    @Query(value = "SELECT * FROM consent_contents WHERE released_at::DATE = CAST(:dateParam AS timestamp) and status_id = 'scheduled'", nativeQuery = true)
    List<ConsentContent> listConsentContentStatusIdPublished(@Param("dateParam") String dateParam);

    @Query(value = "SELECT cdc_consent_id FROM consents WHERE id = :consentId", nativeQuery = true)
    String getCdcConsentId(@Param("consentId") Long consentId);

    @Query(value = "SELECT case when count(*)=0 then 'y' else 'n' end as result FROM consent_contents WHERE consent_id = :consentId and status_id = 'published'", nativeQuery = true)
    String newConsentContentStatusIdPublished(@Param("consentId") Long consentId);

    @Query(value = "SELECT status_id FROM consent_contents WHERE id = :id", nativeQuery = true)
    String getStatusId(@Param("id") int id);
    
    @Query(value = "select distinct cc.version, cc.id from consents c inner join consent_contents cc on c.id = cc.consent_id where cc.id < 2000 and c.type_id = :typeId and c.coverage = :coverage and c.countries = :countries and cc.language_id = :language ORDER BY cc.version", nativeQuery = true)
    List<ConsentContent> getVersionList(@Param("typeId") String typeId, @Param("coverage") String coverage, @Param("countries") String countries, @Param("language") String language);
    
    @Query("SELECT cc.content FROM ConsentContent cc WHERE cc.id = :id")
    String findContentById(Long id);
}