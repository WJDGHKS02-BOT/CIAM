package com.samsung.ciam.repositories;

import com.samsung.ciam.models.ChannelAdCheck;
import com.samsung.ciam.models.CommonCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChannelAdCheckRepository extends JpaRepository<ChannelAdCheck, Long> {

    @Query(value = "SELECT ad_check_yn FROM channel_ad_check WHERE channel = :channel ", nativeQuery = true)
    String selectChannelAdCheckYn(@Param("channel") String channel);

}

