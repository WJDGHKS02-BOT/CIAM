package com.samsung.ciam.repositories;

import java.util.List;

import com.samsung.ciam.models.WfMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.samsung.ciam.models.ApprovalAdmin;

public interface ApprovalAdminRepository extends JpaRepository<ApprovalAdmin, Long>{
    
     
    // 전체 조건조회 : CB Admin 조회
//    @Query(value = "SELECT id, uid, email, type, (CASE WHEN tempapprover = true THEN 'Temp Approver' ELSE (select name from common_code where header = 'ROLE_CODE' and attribute3 = a.type) END) type_nm, channel, country, subsidiary, division, company_code, active, approval_user, approval_date, tempapprover, tempapprovalrule, tempapprovalrulelevel, disabled, disabledrule, disabledrulelevel, updateuser, TO_CHAR(created_at, 'YYYY-MM-DD') created_at, TO_CHAR(updated_at, 'YYYY-MM-DD') updated_at " +
//                   " FROM public.approval_admins a WHERE type=:type and COALESCE(NULLIF(TRIM(channel), ''), 'ALL')=:channel and COALESCE(NULLIF(TRIM(country), ''), 'ALL')=:country and COALESCE(NULLIF(TRIM(subsidiary), ''), 'ALL')=:subsidiary and COALESCE(NULLIF(TRIM(division), ''), 'ALL')=:division and active=true " +
//                   "  and email not in ( select email from approval_admins aa where disabled = true and disabledrule = :ruleMasterId and disabledrulelevel = :ruleLevel ) and (tempapprover is null or tempapprover != true or (tempapprover = true and tempapprovalrule = :ruleMasterId and tempapprovalrulelevel = :ruleLevel))", nativeQuery = true)
//    List<ApprovalAdmin> selectApprovalAdminList(@Param("type") String type, @Param("channel") String channel, @Param("country") String country, @Param("subsidiary") String subsidiary, @Param("division") String division, @Param("ruleMasterId") String ruleMasterId, @Param("ruleLevel") String ruleLevel);

    @Query(value = "SELECT id, uid, email, type, " +
            "(CASE WHEN tempapprover = true THEN 'Temp Approver' ELSE " +
            "(SELECT name FROM common_code WHERE header = 'ROLE_CODE' AND attribute3 = a.type) END) type_nm, " +
            "channel, country, subsidiary, division, company_code, active, approval_user, approval_date, " +
            "tempapprover, tempapprovalrule, tempapprovalrulelevel, disabled, disabledrule, disabledrulelevel, " +
            "updateuser, TO_CHAR(created_at, 'YYYY-MM-DD') created_at, TO_CHAR(updated_at, 'YYYY-MM-DD') updated_at " +
            "FROM public.approval_admins a " +
            "WHERE type=:type " +
            "AND COALESCE(NULLIF(TRIM(channel), ''), 'ALL') = :channel " +
            "AND (COALESCE(NULLIF(TRIM(country), ''), 'ALL') ~ ('(^|,)' || :country || '(,|$)')) " +
            "AND (COALESCE(NULLIF(TRIM(subsidiary), ''), 'ALL') ~ ('(^|,)' || :subsidiary || '(,|$)')) " +
            "AND (COALESCE(NULLIF(TRIM(division), ''), 'ALL') = :division) " +
            "AND active=true " +
            "AND email NOT IN (SELECT email FROM approval_admins aa WHERE disabled = true " +
            "AND disabledrule = :ruleMasterId AND disabledrulelevel = :ruleLevel) " +
            "AND (tempapprover IS NULL OR tempapprover != true " +
            "OR (tempapprover = true AND tempapprovalrule = :ruleMasterId AND tempapprovalrulelevel = :ruleLevel))",
            nativeQuery = true)
    List<ApprovalAdmin> selectApprovalAdminList(
            @Param("type") String type,
            @Param("channel") String channel,
            @Param("country") String country,
            @Param("subsidiary") String subsidiary,
            @Param("division") String division,
            @Param("ruleMasterId") String ruleMasterId,
            @Param("ruleLevel") String ruleLevel);

    // Type과 Channel 조건조회 : PA 또는 CA 조회
    @Query(value = "SELECT id, uid, email, type, (CASE WHEN tempapprover = true THEN 'Temp Approver' ELSE (select name from common_code where header = 'ROLE_CODE' and attribute3 = a.type) END) type_nm, channel, country, subsidiary, division, company_code, active, approval_user, approval_date, tempapprover, tempapprovalrule, tempapprovalrulelevel, disabled, disabledrule, disabledrulelevel, updateuser, TO_CHAR(created_at, 'YYYY-MM-DD') created_at, TO_CHAR(updated_at, 'YYYY-MM-DD') updated_at " +
                   " FROM public.approval_admins a WHERE type=:type and COALESCE(NULLIF(TRIM(channel), ''), 'ALL')=:channel and active=true " +
                   "  and email not in ( select email from approval_admins aa where disabled = true and disabledrule = :ruleMasterId and disabledrulelevel = :ruleLevel ) and (tempapprover is null or tempapprover != true or (tempapprover = true and tempapprovalrule = :ruleMasterId and tempapprovalrulelevel = :ruleLevel))", nativeQuery = true)
    List<ApprovalAdmin> selectApprovalAdminList(@Param("type") String type, @Param("channel") String channel, @Param("ruleMasterId") String ruleMasterId, @Param("ruleLevel") String ruleLevel);
    
    // Type만 조회 : AM 조회
    @Query(value = "SELECT id, uid, email, type, (CASE WHEN tempapprover = true THEN 'Temp Approver' ELSE (select name from common_code where header = 'ROLE_CODE' and attribute3 = a.type) END) type_nm, channel, country, subsidiary, division, company_code, active, approval_user, approval_date, tempapprover, tempapprovalrule, tempapprovalrulelevel, disabled, disabledrule, disabledrulelevel, updateuser, TO_CHAR(created_at, 'YYYY-MM-DD') created_at, TO_CHAR(updated_at, 'YYYY-MM-DD') updated_at " +
                   " FROM public.approval_admins a WHERE type=:type and active=true " +
                   "  and email not in ( select email from approval_admins aa where disabled = true and disabledrule = :ruleMasterId and disabledrulelevel = :ruleLevel ) and (tempapprover is null or tempapprover != true or (tempapprover = true and tempapprovalrule = :ruleMasterId and tempapprovalrulelevel = :ruleLevel))", nativeQuery = true)
    List<ApprovalAdmin> selectApprovalAdminList(@Param("type") String type, @Param("ruleMasterId") String ruleMasterId, @Param("ruleLevel") String ruleLevel);

    // 일반 insert
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO approval_admins (id, uid, email, type, channel, country, subsidiary, division, company_code, active, approval_user, approval_date, updateuser, created_at, updated_at) " +
                   " VALUES(nextval('approval_admins_id_seq'::regclass), :uid, :email, :type, :channel, :country, :subsidiary, :division, :companyCode, true, :approvalUser, (NOW() AT TIME ZONE 'UTC' + INTERVAL '9 hours'), null, (NOW() AT TIME ZONE 'UTC' + INTERVAL '9 hours'), null)", nativeQuery = true)
    int insertApprovalAdmin(@Param("uid") String uid, @Param("email") String email, @Param("type") String type, @Param("channel") String channel, @Param("country") String country, @Param("subsidiary") String subsidiary, @Param("division") String division,
                           @Param("companyCode") String companyCode, @Param("approvalUser") String approvalUser);

    // TA insert (Type이 'TA'일때)
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO approval_admins (id, uid, email, type, channel, country, subsidiary, division, company_code, active, approval_user, approval_date, tempapprover, tempapprovalrule, tempapprovalrulelevel, updateuser, created_at, updated_at) " +
                    " VALUES(nextval('approval_admins_id_seq'::regclass), :uid, :email, :type, :channel, :country, :subsidiary, :division, :companyCode, true, :approvalUser, (NOW() AT TIME ZONE 'UTC' + INTERVAL '9 hours'), true, :ruleMasterId, :ruleLevel, null, (NOW() AT TIME ZONE 'UTC' + INTERVAL '9 hours'), null)", nativeQuery = true)
    int insertApprovalAdmin(@Param("uid") String uid, @Param("email") String email, @Param("type") String type, @Param("channel") String channel, @Param("country") String country, @Param("subsidiary") String subsidiary, @Param("division") String division,
                            @Param("companyCode") String companyCode, @Param("approvalUser") String approvalUser, @Param("ruleMasterId") String ruleMasterId, @Param("ruleLevel") String ruleLevel);

    // 비활성 데이터 있으면 delete
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM approval_admins WHERE email=:email and type=:type and channel=:channel and country=:country and subsidiary=:subsidiary and division=:division and disabled=true", nativeQuery = true)
    int deleteDisabledApprovalAdmin(@Param("email") String email, @Param("type") String type, @Param("channel") String channel, @Param("country") String country, @Param("subsidiary") String subsidiary, @Param("division") String division);

    // 삭제시 비활성 데이터 추가
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO approval_admins (id, uid, email, type, channel, country, subsidiary, division, company_code, active, approval_user, approval_date, tempapprover, tempapprovalrule, tempapprovalrulelevel, disabled, disabledrule, disabledrulelevel, updateuser, created_at) " +
                   "SELECT nextval('approval_admins_id_seq'::regclass) id, uid, email, type, channel, country, subsidiary, division, company_code, false, approval_user, approval_date, tempapprover, tempapprovalrule, tempapprovalrulelevel, true, :ruleMasterId, :ruleLevel, :updateuser, (NOW() AT TIME ZONE 'UTC' + INTERVAL '9 hours') created_at " +
                   "  FROM approval_admins WHERE id = :id", nativeQuery = true)
    int insertDisabledApprovalAdmin(@Param("id") int id, @Param("ruleMasterId") String ruleMasterId, @Param("ruleLevel") String ruleLevel, @Param("updateuser") String updateuser);

    @Query(value = "SELECT count(*) FROM approval_admins WHERE uid = :uid and type=:type and channel=:channel and country=:country and subsidiary=:subsidiary and division=:division and active=true", nativeQuery = true)
    int selectDuplicateApprovalAdmin(@Param("uid") String uid, @Param("type") String type, @Param("channel") String channel, @Param("country") String country, @Param("subsidiary") String subsidiary, @Param("division") String division);

    @Modifying
    @Transactional
    @Query(value = "UPDATE approval_admins SET active = false WHERE uid = :uid AND channel = :channel", nativeQuery = true)
    void updateFalseStatus(@Param("uid") String uid, @Param("channel") String channel);

    @Modifying
    @Transactional
    @Query(value = "UPDATE approval_admins SET company_code = :companyCode WHERE uid = :uid AND active = true", nativeQuery = true)
    void updateApprovalCompanyCode(@Param("uid") String uid,@Param("companyCode") String companyCode);

    @Modifying
    @Transactional
    @Query(value = "UPDATE approval_admins SET active = false WHERE uid = :uid", nativeQuery = true)
    void updateUidFalseStatus(@Param("uid") String uid);

    //  결재라인 Search Channel Admin
    @Query(value = "SELECT subsidiary FROM approval_admins wm WHERE wm.uid = :uid AND channel =:channel ", nativeQuery = true)
    String selectApprovalAdminSubsidiary(@Param("uid") String uid,@Param("channel") String channel);

    //  결재라인 Search Channel Admin
    @Query(value = "SELECT country FROM approval_admins wm WHERE wm.uid = :uid AND channel =:channel ", nativeQuery = true)
    String selectApprovalAdminCountry(@Param("uid") String uid,@Param("channel") String channel);

    @Query(value = "SELECT unnest(string_to_array(aa.subsidiary, ',')) AS subsidiary " +
            "FROM approval_admins aa " +
            "WHERE aa.uid = :uid AND aa.channel = :channel", nativeQuery = true)
    List<String> selectBizAdminSubsidiary(@Param("uid") String uid,@Param("channel") String channel);
}