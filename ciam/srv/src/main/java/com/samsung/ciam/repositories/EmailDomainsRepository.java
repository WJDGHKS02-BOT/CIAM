package com.samsung.ciam.repositories;

import com.samsung.ciam.models.EmailDomains;
import com.samsung.ciam.models.Channels;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EmailDomainsRepository extends JpaRepository<EmailDomains, Long> {

    @Query(value = "SELECT * FROM email_domains WHERE domain LIKE :domain AND type LIKE 'banned'LIMIT 1", nativeQuery = true)
    List<EmailDomains> selectBannedDomains(@Param("domain") String domain);

    @Query(value = "SELECT * FROM email_domains WHERE type = 'banned' AND channel = :channel", nativeQuery = true)
    List<EmailDomains> selectBannedDomainsList(@Param("channel") String channel);
}
