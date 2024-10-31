package com.samsung.ciam.repositories;

import com.samsung.ciam.models.WfList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WfListRepository extends JpaRepository<WfList, String> {

    @Query(value = "SELECT * FROM wf_list caf WHERE caf.wf_id = :wfId", nativeQuery = true)
    List<WfList> selectWfList(@Param("wfId") String wfId);
}
