package com.samsung.ciam.repositories;

import com.samsung.ciam.models.BtpAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BtpAuditLogRepository extends JpaRepository<BtpAuditLog, Long> {
    // insert & update only
}
