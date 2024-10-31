package com.samsung.ciam.repositories;

import com.samsung.ciam.models.ErrorLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ErrorLogRepository extends JpaRepository<ErrorLog, Long> {
}
