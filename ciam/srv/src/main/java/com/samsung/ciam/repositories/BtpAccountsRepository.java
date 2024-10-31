package com.samsung.ciam.repositories;

import com.samsung.ciam.models.BtpAccounts;
import com.samsung.ciam.models.NewCompany;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BtpAccountsRepository extends JpaRepository<BtpAccounts, Long> {

    // search by Company Name(like)
    public List<BtpAccounts> findByNameLikeIgnoreCaseOrderByNameAsc(String name);

    @Query(value = "SELECT * FROM btp_accounts WHERE bpid = :bpid LIMIT 1", nativeQuery = true)
    BtpAccounts selectByBpid(@Param("bpid") String bpid);

    Optional<BtpAccounts> findByBpid(String text);
}
