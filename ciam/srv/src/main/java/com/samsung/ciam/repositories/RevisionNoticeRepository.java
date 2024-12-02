package com.samsung.ciam.repositories;

import com.samsung.ciam.models.CommonCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RevisionNoticeRepository extends JpaRepository<CommonCode, Long>{

    @Query(value = "SELECT id, revision_title, revision_contents, language_id, status, type, " +
            "TO_CHAR(apply_at, 'YYYY-MM-DD HH24:MI:SS') as apply_at, " +
            "TO_CHAR(created_at, 'YYYY-MM-DD HH24:MI:SS') as created_at, " +
            "TO_CHAR(updated_at, 'YYYY-MM-DD HH24:MI:SS') as updated_at, " +
            "channel, subsidiary " +
            "FROM revision_notice WHERE id = :id", nativeQuery = true)
    List<Object[]> findRevisionById(@Param("id") Long id);


}
