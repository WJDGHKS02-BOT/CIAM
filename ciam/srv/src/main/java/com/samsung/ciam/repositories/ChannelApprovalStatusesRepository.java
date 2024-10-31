package com.samsung.ciam.repositories;

import com.samsung.ciam.models.ChannelApprovalStatuses;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChannelApprovalStatusesRepository extends JpaRepository<ChannelApprovalStatuses, Long> {

    @Query(value = "SELECT * FROM channel_approval_statuses WHERE login_uid = :loginUid AND channel = :channel AND status = :status ORDER BY approval_line DESC LIMIT 1", nativeQuery = true)
    Optional<ChannelApprovalStatuses> selectFirstByLoginUidAndChannelAndStatus(@Param("loginUid") String loginUid, @Param("channel") String channel, @Param("status") String status);

    @Query(value = "SELECT * FROM channel_approval_statuses WHERE channel = :channel AND status = :status LIMIT 1", nativeQuery = true)
    Optional<ChannelApprovalStatuses> selectFirstByChannelAndStatus(@Param("channel") String channel, @Param("status") String status);

    @Query(value = "SELECT * FROM channel_approval_statuses WHERE login_id = :loginId AND status = :status", nativeQuery = true)
    List<ChannelApprovalStatuses> selectByLoginIdAndStatus(@Param("loginId") String loginId, @Param("status") String status);

    @Query(value = "SELECT COUNT(*) FROM channel_approval_statuses WHERE channel = :channel AND status = :status", nativeQuery = true)
    long countByChannelAndStatus(@Param("channel") String channel, @Param("status") String status);
}