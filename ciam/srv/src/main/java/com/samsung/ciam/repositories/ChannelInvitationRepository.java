package com.samsung.ciam.repositories;

import com.samsung.ciam.models.ChannelInvitation;
import com.samsung.ciam.models.ConsentJConsentContents;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChannelInvitationRepository extends JpaRepository<ChannelInvitation, Long> {

    @Query(value = "SELECT * FROM channel_invitations " +
            "WHERE bpid LIKE :accountId AND channel LIKE :channel " +
            "AND (CASE WHEN :status = 'all' THEN TRUE ELSE status = :status END) " +
            "AND (:searchText IS NULL OR login_id LIKE %:searchText%) " +
            "LIMIT COALESCE(NULLIF(:pageRow, '')::INTEGER, 1000)", nativeQuery = true)
    List<ChannelInvitation> selectPartnerAdminInvitationList(@Param("accountId") String accountId,
                                                             @Param("status") String status,
                                                             @Param("channel") String channel,
                                                             @Param("searchText") String searchText,
                                                             @Param("pageRow") String pageRow);

    @Query(value = "SELECT * FROM channel_invitations " +
            "WHERE channel LIKE :channel " +
            "AND (CASE WHEN :subsidiary IS NULL OR :subsidiary = '' OR :subsidiary = 'ALL' " +
            "THEN TRUE " +
            "ELSE COALESCE(NULLIF(TRIM(subsidiary), ''), 'ALL') ~ ('(^|,)' || REPLACE(:subsidiary, ',', '|') || '(,|$)') END) " +
            "AND (CASE WHEN :status = 'all' THEN TRUE ELSE status = :status END) " +
            "AND (:searchText IS NULL OR login_id LIKE %:searchText%) " +
            "LIMIT COALESCE(NULLIF(:pageRow, '')::INTEGER, 1000)", nativeQuery = true)
    List<ChannelInvitation> selectChannelBizAdminInvitationList(@Param("channel") String channel,
                                                                @Param("status") String status,
                                                                @Param("subsidiary") String subsidiary,
                                                                @Param("searchText") String searchText,
                                                                @Param("pageRow") String pageRow);

    @Query(value = "SELECT * FROM channel_invitations " +
            "WHERE channel LIKE :channel " +
            "AND (CASE WHEN :status = 'all' THEN TRUE ELSE status = :status END) " +
            "AND (:searchText IS NULL OR login_id LIKE %:searchText%) " +
            "LIMIT COALESCE(NULLIF(:pageRow, '')::INTEGER, 1000)", nativeQuery = true)
    List<ChannelInvitation> selectChannelAdminInvitationList(@Param("channel") String channel,
                                                             @Param("status") String status,
                                                             @Param("searchText") String searchText,
                                                             @Param("pageRow") String pageRow);

    @Query(value = "SELECT * FROM channel_invitations " +
            "WHERE (CASE WHEN :status = 'all' THEN TRUE ELSE status = :status END) " +
            "AND (:searchText IS NULL OR login_id LIKE %:searchText%) " +
            "LIMIT COALESCE(NULLIF(:pageRow, '')::INTEGER, 1000)", nativeQuery = true)
    List<ChannelInvitation> selectCiamAdminAdminInvitationList(@Param("status") String status,
                                                               @Param("searchText") String searchText,
                                                               @Param("pageRow") String pageRow);

    @Query(value = "SELECT * FROM channel_invitations WHERE token = :token", nativeQuery = true)
    Optional<ChannelInvitation> findByToken(@Param("token") String token);

    @Query(value = "SELECT * FROM channel_invitations ci WHERE status= 'pending' AND expiry < NOW() order by expiry asc", nativeQuery = true)
    List<ChannelInvitation> selectInvitationExpiryList();

    // bpid가 특정 값이고 status가 특정 값인 데이터를 찾는 쿼리
    @Query("SELECT ci FROM ChannelInvitation ci WHERE ci.bpid = :bpid AND ci.status = :status")
    List<ChannelInvitation> findByBpidAndStatus(@Param("bpid") String bpid, @Param("status") String status);
}
