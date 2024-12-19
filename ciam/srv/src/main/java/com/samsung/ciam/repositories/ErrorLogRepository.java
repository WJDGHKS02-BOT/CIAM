package com.samsung.ciam.repositories;

import com.samsung.ciam.models.ErrorLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

public interface ErrorLogRepository extends JpaRepository<ErrorLog, Long> {
    @Query(value = "SELECT * FROM error_log " +
            "WHERE (created_at >= COALESCE(CAST(:startDate AS TIMESTAMP), created_at)) " +
            "AND (created_at <= COALESCE(CAST(:endDate AS TIMESTAMP), created_at)) " +
            "AND (:errorMessage IS NULL OR message LIKE CAST(:errorMessage AS TEXT)) " +
            "AND (:completionFlag IS NULL OR " +
            "     (completion_flag = CAST(:completionFlag AS BOOLEAN) AND :completionFlag IS NOT NULL) OR " +
            "     (completion_flag IS NULL AND :completionFlag = 'false')) " +
            "ORDER BY created_at DESC",
            nativeQuery = true)
    List<ErrorLog> searchErrorLogs(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("errorMessage") String errorMessage,
            @Param("completionFlag") String completionFlag
    );
}
