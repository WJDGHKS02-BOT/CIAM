package com.samsung.ciam.repositories;

import com.samsung.ciam.models.VerificationEmploy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.Modifying;

import java.util.*;
import java.util.Optional;

public interface VerificationEmployRepository  extends JpaRepository<VerificationEmploy, Long> {
    // channel로 verification_employ 찾는 메소드
    @Query(value = "SELECT id, channel, bpid, bp_name, requestor_uid, requestor_email, requestor_role, TO_CHAR(request_date, 'YYYY-MM-DD') as request_date,"+
        " TO_CHAR(request_expired_date, 'YYYY-MM-DD') as request_expired_date, TO_CHAR(approved_date, 'YYYY-MM-DD') as approved_date,  TO_CHAR(rejected_date, 'YYYY-MM-DD') as rejected_date, approver_email, approver_uid, approver_role, reject_reason,"+
        " view_status, reject_id, reject_email, user_status FROM verification_employ WHERE view_status = 'open' AND channel like :channel AND user_status = :type", nativeQuery = true)
    List<Map<String, Object>> searchVerificationEmploy(@Param("channel") String channel, @Param("type") String type);

    // PA 이면 Company ID로 조회
    @Query(value = "SELECT id, channel, bpid, bp_name, requestor_uid, requestor_email, requestor_role, TO_CHAR(request_date, 'YYYY-MM-DD') as request_date,"+
        " TO_CHAR(request_expired_date, 'YYYY-MM-DD') as request_expired_date, TO_CHAR(approved_date, 'YYYY-MM-DD') as approved_date,  TO_CHAR(rejected_date, 'YYYY-MM-DD') as rejected_date, approver_email, approver_uid, approver_role, reject_reason,"+
        " view_status, reject_id, reject_email, user_status FROM verification_employ WHERE view_status = 'open' AND channel like :channel AND user_status = :type "+
        " and bpid = :cdc_companyid AND requestor_role !='Partner Admin' "        
        , nativeQuery = true)
    List<Map<String, Object>> searchVerificationEmploy(@Param("channel") String channel , @Param("type") String type,  @Param("cdc_companyid") String cdc_companyid);

    // Channel biz Admin 이면 subsidiary로 조회
    @Query(value = "SELECT id, channel, bpid, bp_name, requestor_uid, requestor_email, requestor_role, TO_CHAR(request_date, 'YYYY-MM-DD') as request_date,"+
        " TO_CHAR(request_expired_date, 'YYYY-MM-DD') as request_expired_date, TO_CHAR(approved_date, 'YYYY-MM-DD') as approved_date,  TO_CHAR(rejected_date, 'YYYY-MM-DD') as rejected_date, approver_email, approver_uid, approver_role, reject_reason,"+
        " view_status, reject_id, reject_email, user_status FROM verification_employ WHERE view_status = 'open' AND channel like :channel AND user_status = :type AND "+
        " subsidiary IN ( select subsidiary from approval_admins aa where uid =:uid and active = true and channel like :channel );"        
        , nativeQuery = true)
    List<Map<String, Object>> searchVerificationEmployCB(@Param("channel") String channel , @Param("type") String type, @Param("uid") String uid);


    // Mail 발송을 위한 UID
    @Query(value = "select DISTINCT  uid, channel  from approval_admins aa where active = true and type in ('PA','CB','CA') ; "        
    //SELECT uid, MIN(channel) AS channel FROM approval_admins aa WHERE active = true AND "type" IN ('PA', 'CB', 'CA') GROUP BY uid; 
        , nativeQuery = true)
    List<Map<String, Object>> insertBatchMail();

    // bpid로 VerificationDomain을 찾는 메소드
    List<VerificationEmploy> findByBpid(String bpid);
    
    // bpid로 VerificationDomain을 찾는 쿼리
//    @Query(value = "SELECT * FROM verification_domains WHERE bpid = :bpid", nativeQuery = true)
    //List<VerificationDomain> findByBpidNative(@Param("bpid") String bpid);
    @Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END FROM verification_domains WHERE bpid = :bpid and email_domain =:email_domain and status ='approved' " , 
            nativeQuery = true)
    boolean isSameEmailDomain(@Param("bpid") String bpid, @Param("email_domain") String email_domain);

    @Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END FROM verification_domains WHERE bpid = :bpid and email_domain =:email_domain and status ='pending' " , 
    nativeQuery = true)
    boolean isPendingEmailDomain(@Param("bpid") String bpid, @Param("email_domain") String email_domain);

    @Modifying
    @Transactional
    @Query(value = 
        "WITH status_check AS (" +
            "    SELECT CASE WHEN EXISTS (" +
            "        SELECT 1 FROM verification_domains vd " +
            "        WHERE vd.status = 'approved' AND vd.bpid = :account_id  " + //AND vd.channel = :channel 2024.09.24 채널은 조회조건에서 제외!
            "        AND SUBSTRING(:username FROM POSITION('@' IN :username) + 1) = vd.email_domain" +
            "    ) THEN 'approved' ELSE 'pending' END AS user_status" +
            ")" +
            "INSERT INTO verification_employ " +
            "(requestor_uid, bpid, channel, requestor_email, bp_name, user_status, view_status, request_date, request_expired_date, if_yyyymm, requestor_role, cdc_status, approved_date, approver_uid, approver_email, subsidiary, division) " +
            "VALUES " +
            "(:uid, :account_id, :channel, :username, :org_name, " +
            "(SELECT user_status FROM status_check), 'open', NOW() AT TIME ZONE 'UTC' + INTERVAL '9 hours', " +
            "NOW() AT TIME ZONE 'UTC' + INTERVAL '9 hours' + INTERVAL '1 month'- INTERVAL '1 day', " +
            "TO_CHAR(NOW() AT TIME ZONE 'UTC' + INTERVAL '9 hours', 'YYYYMM'), :role, :cdc_status, " +
            "(CASE WHEN (SELECT user_status FROM status_check) = 'approved' THEN NOW() AT TIME ZONE 'UTC' + INTERVAL '9 hours' ELSE NULL END), " +
            "(CASE WHEN (SELECT user_status FROM status_check) = 'approved' THEN 'auto' ELSE NULL END), " +
            "(CASE WHEN (SELECT user_status FROM status_check) = 'approved' THEN 'auto' ELSE NULL END), :subsidiary, :division ) "
    , nativeQuery = true)
    void insertVerificationEmploy(
        @Param("uid") String uid, 
        @Param("account_id") String accountId, 
        @Param("channel") String channel, 
        @Param("username") String username, 
        @Param("org_name") String orgName, 
        @Param("role") String role,
        @Param("cdc_status") String cdc_status,
        @Param("subsidiary") String subsidiary,
        @Param("division") String division
    );


    @Modifying
    @Transactional
    @Query(value = 
            "INSERT INTO verification_employ " +
            "(requestor_uid, bpid, channel, requestor_email, bp_name, user_status, view_status, request_date, request_expired_date, if_yyyymm, requestor_role, cdc_status,subsidiary, division) " +
            "VALUES " +
            "(:uid, :account_id, :channel, :username, :org_name, 'pending' , " +
            "'open', NOW() AT TIME ZONE 'UTC' + INTERVAL '9 hours', " +
            "NOW() AT TIME ZONE 'UTC' + INTERVAL '9 hours' + INTERVAL '1 month'- INTERVAL '1 day' , " +
            "TO_CHAR(NOW() AT TIME ZONE 'UTC' + INTERVAL '9 hours', 'YYYYMM'), :role, :cdc_status, :subsidiary, :division )"  , nativeQuery = true)
    void insertVerificationEmployPA(
        @Param("uid") String uid, 
        @Param("account_id") String accountId, 
        @Param("channel") String channel, 
        @Param("username") String username, 
        @Param("org_name") String orgName, 
        @Param("role") String role,
        @Param("cdc_status") String cdc_status,
        @Param("subsidiary") String subsidiary,
        @Param("division") String division
    );


    
    @Modifying
    @Transactional
    @Query(value = 
        "UPDATE verification_employ SET user_status = :new_status,  " + 
        "approver_uid= CASE WHEN :new_status = 'approved' THEN :approver_uid ELSE NULL END , "+
        "approver_email= CASE WHEN :new_status = 'approved' THEN :approver_email ELSE NULL END , "+
        "reject_id =    CASE WHEN :new_status = 'rejected' THEN :approver_email ELSE NULL END , "+
        "reject_email=  CASE WHEN :new_status = 'rejected' THEN :approver_email ELSE NULL END , "+
        "approved_date = CASE WHEN :new_status = 'approved' THEN NOW() AT TIME ZONE 'UTC' + INTERVAL '9 hours' ELSE approved_date END, " +
        "rejected_date = CASE WHEN :new_status = 'rejected' THEN NOW() AT TIME ZONE 'UTC' + INTERVAL '9 hours' ELSE rejected_date END, " +
        "reject_reason = CASE WHEN :new_status = 'rejected' THEN :reject_reason ELSE NULL END " + 
        "WHERE requestor_uid = :requestor_uid AND id = :id", 
        nativeQuery = true)
    int updateUserStatus(
        @Param("new_status") String newStatus,
        @Param("reject_reason") String reject_reason,
        @Param("requestor_uid") String requestorUid,
        @Param("id") int id,
        @Param("approver_uid") String approver_uid,
        @Param("approver_email") String approver_email
    );

    @Modifying
    @Transactional
    @Query(value = 
        "UPDATE verification_employ v SET view_status = 'close' where view_status='open' and user_status !='rejected' "
        , nativeQuery = true)
    void updateViewStatus();

    @Modifying
    @Transactional
    @Query(value = 
        "UPDATE verification_employ SET user_status = 'rejected' , reject_id = 'auto', reject_email = 'auto' ,  "+
        "rejected_date = NOW() AT TIME ZONE 'UTC' + INTERVAL '9 hours' , " +
        "reject_reason = 'Auto Expired' " + 
        "WHERE user_status = 'pending' AND view_status ='open' and request_expired_date <= CAST(:formattedToday AS TIMESTAMP)  "+
        "RETURNING requestor_uid, requestor_email, channel, bpid, subsidiary "
        , nativeQuery = true)
    List<Map<String, Object>>  expireVerificationEmploy(  @Param("formattedToday") String formattedToday );


    // 재직인증 기한 마감 Mail 발송시 담당자를 가져오는 로직 1) PA
    @Query(value = "select DISTINCT email from approval_admins aa where active = true and type in ('PA') and channel =:channel and company_code = :bpid ;  "        
        , nativeQuery = true)
    List<Map<String, Object>> searchVerificationEND_PA(@Param("channel") String channel,  @Param("bpid") String bpid);

    // 재직인증 기한 마감 Mail 발송시 담당자를 가져오는 로직 2) CB
    @Query(value = "select DISTINCT email from approval_admins aa where active = true and type in ('CB') and channel =:channel and aa.subsidiary =:subsidiary  ;  "        
        , nativeQuery = true)
    List<Map<String, Object>> searchVerificationEND_CB(@Param("channel") String channel,  @Param("subsidiary") String subsidiary);

    // 재직인증 기한 마감 Mail 발송시 담당자를 가져오는 로직 3) CA
    @Query(value = "select DISTINCT email from approval_admins aa where active = true and type in ('CA') and channel =:channel  "        
        , nativeQuery = true)
    List<Map<String, Object>> searchVerificationEND_CA(@Param("channel") String channel );

}