package com.samsung.ciam.repositories;

import com.samsung.ciam.models.ChannelApprovers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChannelApproversRepository extends JpaRepository<ChannelApprovers, Long> {

    @Query(value = "SELECT * FROM channel_approvers WHERE channel = :channel AND auto_approve = true LIMIT 1", nativeQuery = true)
    Optional<ChannelApprovers> selectAutoApprove(@Param("channel") String channel);

    @Query(value = "SELECT * FROM channel_approvers WHERE channel = :channel AND auto_approve = false AND approval_line = 1 ORDER BY rule ASC", nativeQuery = true)
    List<ChannelApprovers> selectLevel1Approvers(@Param("channel") String channel);

    @Query(value = "SELECT COUNT(*) FROM channel_approvers WHERE channel = :channel AND auto_approve = false AND approval_line = 1", nativeQuery = true)
    long countLevel1Approvers(@Param("channel") String channel);

    @Query(value = "SELECT * FROM channel_approvers WHERE channel LIKE :channel AND auto_approve = true AND approval_line = 1 LIMIT 1", nativeQuery = true)
    ChannelApprovers selectApprovers(@Param("channel") String channel);

    /*@Query("SELECT ca FROM ChannelApprovers ca WHERE ca.channel LIKE :channel AND ca.autoApprove = true")
    ChannelApprovers findFirstByChannelAndAutoApprove(@Param("channel") String channel, boolean autoApprove);*/

    /*@Query("SELECT ca FROM ChannelApprovers ca WHERE ca.channel LIKE :channel AND ca.autoApprove = false AND ca.approvalLine = :approvalLine ORDER BY ca.rule ASC")
    List<ChannelApprovers> findByChannelAndAutoApproveAndApprovalLineOrderByRuleAsc(@Param("channel") String channel, boolean autoApprove, int approvalLine);*/
}