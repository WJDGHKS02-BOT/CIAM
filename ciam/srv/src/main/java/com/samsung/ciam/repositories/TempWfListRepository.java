package com.samsung.ciam.repositories;

import com.samsung.ciam.models.TempWfList;
import com.samsung.ciam.models.WfList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TempWfListRepository extends JpaRepository<TempWfList, String> {

    @Query(value = "SELECT * FROM temp_wf_list caf WHERE caf.wf_id = :wfId", nativeQuery = true)
    List<TempWfList> selectWfList(@Param("wfId") String wfId);
}
