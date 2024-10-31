package com.samsung.ciam.repositories;

import com.samsung.ciam.models.ChannelConversion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChannelConversionRepository extends JpaRepository<ChannelConversion, Long> {

    @Query(value = "SELECT * FROM channel_conversions WHERE channel LIKE :channel AND convert_login_id LIKE :convertLoginId AND status LIKE 'initiated' LIMIT 1", nativeQuery = true)
    ChannelConversion selectFirstByChannelAndConvertLoginIdAndStatus(
            @Param("channel") String channel,
            @Param("convertLoginId") String convertLoginId
    );

    @Query("SELECT c FROM ChannelConversion c WHERE c.convertLoginId = :convertLoginId")
    List<ChannelConversion> selectByConvertLoginId(@Param("convertLoginId") String convertLoginId);

    @Query("SELECT c FROM ChannelConversion c WHERE c.status = :status")
    List<ChannelConversion> selectByStatus(@Param("status") String status);

    @Query(value = "SELECT * FROM channel_conversions WHERE channel LIKE :channel AND \"convertLoginId\" LIKE :convertLoginId LIMIT 1", nativeQuery = true)
    ChannelConversion selectFirstConversion(
            @Param("channel") String channel,
            @Param("convertLoginId") String convertLoginId
    );
}