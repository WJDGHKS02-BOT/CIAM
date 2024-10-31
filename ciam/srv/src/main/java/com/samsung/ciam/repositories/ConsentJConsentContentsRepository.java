package com.samsung.ciam.repositories;

import com.samsung.ciam.models.ConsentJConsentContents;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ConsentJConsentContentsRepository extends JpaRepository<ConsentJConsentContents, Long> {
    @Query(value = "select a.*, b.consent_id, b.language_id, b.version, b.released_at, b.status_id from consents a join consent_contents b on a.id = b.consent_id "+
                   " where a.coverage in (:channel,'common')"+
                   " order by consent_id desc", nativeQuery = true)
    List<ConsentJConsentContents> selectConsentManagerList(@Param("channel") String channel);

    @Query(value = "select a.*, b.consent_id, b.language_id, b.version, b.released_at, b.status_id from consents a join consent_contents b on a.id = b.consent_id "+
                   " where a.coverage in (:channel,'common') and ('' = :type or a.type_id = :type) and ('' like '%' || :location || '%' or a.countries like '%' || :location || '%') order by consent_id desc", nativeQuery = true)
    List<ConsentJConsentContents> selectConsentManagerList(@Param("channel") String channel, @Param("type") String type, @Param("location") String location);
}
