package com.samsung.ciam.repositories;

import com.samsung.ciam.models.Channels;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChannelRepository extends JpaRepository<Channels, Long>{

    @Query(value = "SELECT * FROM channels WHERE channel_name = :channel LIMIT 1", nativeQuery = true)
    List<Channels> selectChannelName(@Param("channel") String channelName);

    @Query(value = "SELECT config FROM channels WHERE channel_name = :channel LIMIT 1", nativeQuery = true)
    List<Channels> selectChannelConifg( @Param("channel")String channelName);

    @Query(value = "SELECT channel_name FROM channels WHERE config ->> 'sfdc_relevant' = 'true'", nativeQuery = true)
    List<Channels> findSfdcRelevantChannels();

    @Query("SELECT c FROM Channels c WHERE c.channelName = :channelName")
    Optional<Channels> findByChannelName(@Param("channelName") String channelName);

    @Query("SELECT c FROM Channels c WHERE c.channelName = :externalId")
    Optional<Channels> findByRegSouce(@Param("externalId") String externalId);

    @Query(value = "SELECT config FROM channels WHERE channel_name = :channel LIMIT 1", nativeQuery = true)
    List<Channels> selectChannelConfig(@Param("channel") String channelName);

    @Query(value = "SELECT channel_name FROM channels WHERE config ->> 'sfdc_relevant' = 'true'", nativeQuery = true)
    List<Channels> selectSfdcRelevantChannels();

    @Query(value = "SELECT * FROM channels WHERE channel_name = :channelName", nativeQuery = true)
    Optional<Channels> selectByChannelName(@Param("channelName") String channelName);

    @Query(value = "SELECT * FROM channels WHERE channel_name LIKE %:channelName%", nativeQuery = true)
    List<Channels> selectByChannelNameContaining(@Param("channelName") String channelName);

    @Query(value = "SELECT * FROM channels WHERE channel_name = :channelName LIMIT 1", nativeQuery = true)
    Optional<Channels> findChannelByName(@Param("channelName") String channelName);

    @Query(value = "SELECT channel_type FROM channels WHERE cmdm_regch = :regSource LIMIT 1", nativeQuery = true)
    String selectChannelType(@Param("regSource") String regSource);

    @Query(value = "SELECT * FROM channels WHERE channel_type LIKE %:channelType%", nativeQuery = true)
    List<Channels> selectChannelTypeList(@Param("channelType") String channelType);

    @Query(value = "SELECT channel_type FROM channels WHERE channel_name = :channel LIMIT 1", nativeQuery = true)
    String selectChannelTypeSearch(@Param("channel") String channel);

    @Query(value = "SELECT channel_display_name FROM channels WHERE channel_name LIKE %:channelName%", nativeQuery = true)
    String selectChannelDisplayName(@Param("channelName") String channelName);

    @Query(value = "SELECT * FROM channels", nativeQuery = true)
    List<Channels> findAllBy();

    @Query(value = "SELECT cmdm_regch FROM channels WHERE channel_name = :channel LIMIT 1", nativeQuery = true)
    String selectChannelRegCh(@Param("channel") String channel);
}
