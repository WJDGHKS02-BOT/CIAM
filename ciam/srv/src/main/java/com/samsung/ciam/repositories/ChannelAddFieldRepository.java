package com.samsung.ciam.repositories;

import com.samsung.ciam.models.ChannelAddField;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChannelAddFieldRepository extends JpaRepository<ChannelAddField, Long> {

    @Query(value = "SELECT * FROM channel_add_field caf WHERE caf.channel = :channel AND caf.tab_type = :tabType AND caf.add_type = :addType AND caf.half_Type = :halfType ORDER BY caf.display_seq ASC", nativeQuery = true)
    List<ChannelAddField> selectFieldList(@Param("channel") String channel, @Param("tabType") String tabType,@Param("addType") String addType,@Param("halfType") String halfType);

    @Query(value = "SELECT * FROM channel_add_field caf WHERE caf.channel = :channel AND caf.add_type = :addType AND cdc_type = :cdcType", nativeQuery = true)
    List<ChannelAddField> selectAdditionalField(@Param("channel") String channel,@Param("addType") String addType,@Param("cdcType") String cdcType);

    @Query(value = "SELECT * FROM channel_add_field caf WHERE caf.channel = :channel AND caf.tab_type = :tabType", nativeQuery = true)
    List<ChannelAddField> selectFieldList(@Param("channel") String channel,@Param("tabType") String tabType);

    @Query(value = "SELECT * FROM channel_add_field caf WHERE caf.channel = :channel", nativeQuery = true)
    List<ChannelAddField> selectAddFieldList(@Param("channel") String channel);

    @Query(value = "SELECT element_id FROM channel_add_field caf WHERE caf.channel = :channel AND division_yn = 'Y' LIMIT 1", nativeQuery = true)
    String selectDivisionYnField(@Param("channel") String channel);

    @Query(value = "SELECT * FROM channel_add_field caf WHERE caf.channel = :channel AND cdc_type = :cdcType", nativeQuery = true)
    List<ChannelAddField> selectAddFieldListByCdcTypeAndChannel(@Param("cdcType") String cdcType,@Param("channel") String channel);

    @Query(value = "SELECT cdc_data_field FROM channel_add_field caf WHERE caf.channel = :channel AND division_yn = 'Y' LIMIT 1", nativeQuery = true)
    String selectDivisionYnCdcDataField(@Param("channel") String channel);

    @Query(value = "SELECT data_map_id FROM channel_add_field caf WHERE caf.channel = :channel AND division_yn = 'Y' LIMIT 1", nativeQuery = true)
    String selectDivisionYnDataMapId(@Param("channel") String channel);
}
