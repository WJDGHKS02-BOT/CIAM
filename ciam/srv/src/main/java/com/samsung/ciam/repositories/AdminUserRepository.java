package com.samsung.ciam.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.samsung.ciam.models.AdminUser;

public interface AdminUserRepository extends JpaRepository<AdminUser, Long>{
    
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO admin_user (id, uid, email, type, channel, country, subsidiary, division, company_code, active, approval_user, approval_date, updateuser, created_at, updated_at) " +
                   " VALUES(nextval('admin_user_id_seq'::regclass), :uid, :email, :type, :channel, :country, :subsidiary, :division, :companyCode, true, :approvalUser, null, null, (NOW() AT TIME ZONE 'UTC' + INTERVAL '9 hours'), null)", nativeQuery = true)
    int insertAdminUser(@Param("uid") String uid, @Param("email") String email, @Param("type") String type, @Param("channel") String channel, @Param("country") String country, @Param("subsidiary") String subsidiary, @Param("division") String division,
                           @Param("companyCode") String companyCode, @Param("approvalUser") String approvalUser);

    @Query(value = "SELECT count(*) FROM admin_user WHERE uid = :uid", nativeQuery = true)
    int selectDuplicateAdminUser(@Param("uid") String uid);
}
