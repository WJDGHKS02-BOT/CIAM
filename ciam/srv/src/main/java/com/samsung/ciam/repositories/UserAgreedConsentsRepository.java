package com.samsung.ciam.repositories;

import com.samsung.ciam.models.UserAgreedConsents;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserAgreedConsentsRepository extends JpaRepository<UserAgreedConsents, Long> {

    @Query(value = "SELECT consent_id as consentId FROM user_agreed_consents WHERE uid LIKE :uid GROUP BY consent_id", nativeQuery = true)
    List<Long> selectConsentIdsByUid(@Param("uid") String uid);

    @Query(value = "SELECT * FROM user_agreed_consents uac WHERE uac.id IN (SELECT MAX(id) FROM user_agreed_consents WHERE uid LIKE :cdcUid GROUP BY consent_id)", nativeQuery = true)
    List<UserAgreedConsents> selectGroupedConsentsByUid(@Param("cdcUid") String cdcUid);

    @Query(value = "SELECT * FROM user_agreed_consents WHERE consent_id = :consentId AND uid = :uid ORDER BY id DESC LIMIT 1", nativeQuery = true)
    UserAgreedConsents selectUserAgreement(@Param("consentId") Long consentId, @Param("uid") String uid);

    @Query(value = "SELECT * FROM user_agreed_consents WHERE consent_id = :id AND uid = :cdcUid AND consent_content_id = :id ORDER BY id DESC LIMIT 1", nativeQuery = true)
    Optional<UserAgreedConsents> selectConsentId(@Param("id") Long id, @Param("cdcUid") String cdcUid);

    @Query(value = "SELECT * FROM user_agreed_consents WHERE consent_id = :id AND uid = :cdcUid AND consent_content_id = :consentContentId ORDER BY id DESC LIMIT 1", nativeQuery = true)
    Optional<UserAgreedConsents> selectConsentId(@Param("id") Long id, @Param("cdcUid") String cdcUid, @Param("consentContentId") Long consentContentId);

    @Query(value = "SELECT * FROM user_agreed_consents WHERE consent_id = :id", nativeQuery = true)
    Optional<UserAgreedConsents> selectConsentId(@Param("id") Long id);
}