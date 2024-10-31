package com.samsung.ciam.repositories;

import com.samsung.ciam.models.CommonCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CommonCodeRepository extends JpaRepository<CommonCode, Long>{

//    @Query(value = "select * from common_code cc where header =:header", nativeQuery = true)
//    List<CommonCode> selectCommonCodeList(@Param("header") String header);
   List<CommonCode> findByHeader(String header);

   List<CommonCode> findByHeaderOrderBySortOrder(String header);

    @Query(value = "SELECT * FROM common_code cc " +
            "WHERE header = :header AND attribute1 >= ( " +
            "SELECT attribute1 FROM common_code cc2 " +
            "WHERE cc2.value = :role ) AND cc.attribute2='Y'  order by cc.attribute1 desc", nativeQuery = true)
    List<CommonCode> selectRoleList(@Param("header") String header,@Param("role") String role);

    @Query(value = "SELECT name FROM common_code cc " +
            "WHERE header = :header AND code = :role LIMIT 1", nativeQuery = true)
    String selectRoleSearch(@Param("header") String header,@Param("role") String role);

    @Query(value = "SELECT * FROM common_code cc " +
            "WHERE header = :header AND attribute1 >= ( " +
            "SELECT attribute1 FROM common_code cc2 " +
            "WHERE cc2.name = :role )  order by cc.attribute1 desc", nativeQuery = true)
    List<CommonCode> selectRoleNameList(@Param("header") String header,@Param("role") String role);

    @Query(value = "SELECT attribute3 FROM common_code cc " +
            "WHERE header = :header AND code = :role LIMIT 1", nativeQuery = true)
    String selectAttribute3(@Param("header") String header,@Param("role") String role);
}
