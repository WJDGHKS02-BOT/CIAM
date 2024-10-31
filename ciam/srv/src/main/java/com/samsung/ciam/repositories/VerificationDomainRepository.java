package com.samsung.ciam.repositories;

import com.samsung.ciam.models.VerificationDomain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.query.Procedure;

import java.util.*;
import java.util.Map;
import java.util.Optional;

public interface VerificationDomainRepository  extends JpaRepository<VerificationDomain, Long> {
    // channel로 VerificationDomain을 찾는 메소드
    List<VerificationDomain> findByChannel(String channel);

    // bpid로 VerificationDomain을 찾는 메소드
    List<VerificationDomain> findByBpid(String bpid);
    
    // bpid로 VerificationDomain을 찾는 쿼리
//    @Query(value = "SELECT * FROM verification_domains WHERE bpid = :bpid", nativeQuery = true)
    //List<VerificationDomain> findByBpidNative(@Param("bpid") String bpid);
    @Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END FROM verification_domains WHERE bpid = :bpid and email_domain =:email_domain and status ='approved' " , 
            nativeQuery = true)
    boolean isSameEmailDomain(@Param("bpid") String bpid, @Param("email_domain") String email_domain);

    @Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END FROM verification_domains WHERE bpid = :bpid and email_domain =:email_domain and status ='pending' " , 
    nativeQuery = true)
    boolean isPendingEmailDomain(@Param("bpid") String bpid, @Param("email_domain") String email_domain);

    @Query(value = "SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END FROM blacklist_domain WHERE black_domain LIKE %:email_domain%", nativeQuery = true)
    boolean isEmailDomainBlacklisted(@Param("email_domain") String email_domain);


    @Query(value = "select DISTINCT  bpid  FROM verification_domains vd where  vd.status ='approved'  " ,     nativeQuery = true)
    List<Map<String, String>>  EmailDomainList();


    // email_domain, channel, bpid를 인서트하는 쿼리
    @Modifying
    @Transactional
    @Query(value = 
        "INSERT INTO verification_domains " +
        "(wf_id, channel, email_domain, status, bpid, bp_name, requestor_uid, requestor_email, requestor_role, approver_id, approver_email, approver_role, request_date, approved_date) " +
        "VALUES " +
        "(:wf_id, :channel, :email_domain, 'approved', :bpid, :bp_name, :requestor_uid, :requestor_email, :requestor_role, :requestor_uid, :requestor_email, :requestor_role, " +
        "NOW() AT TIME ZONE 'UTC' + INTERVAL '9 hours', NOW() AT TIME ZONE 'UTC' + INTERVAL '9 hours');"+
        "INSERT INTO wf_master " +
        "(wf_id, channel,workflow_code, requestor_id, requestor_email, requestor_role, status, approver_id, approver_email, requested_date, approved_date) " +
        "VALUES " +
        "(:wf_id, :channel,'W07', :requestor_uid, :requestor_email, :requestor_role, 'approved', :requestor_uid, :requestor_email, " +
        "NOW() AT TIME ZONE 'UTC' + INTERVAL '9 hours' , NOW() AT TIME ZONE 'UTC' + INTERVAL '9 hours'); " + 
        "INSERT INTO wf_list " +
        "(wf_id, workflow_code, wf_level , status, approval_format, approver_id, approver_email, approver_role,  approved_date) " +
        "VALUES " +
        "(:wf_id, 'W07', 1, 'approved','auto' , :requestor_uid, :requestor_email, :requestor_role, " +
        "NOW() AT TIME ZONE 'UTC' + INTERVAL '9 hours' ); " +
        "UPDATE verification_employ SET user_status = 'approved', approver_email =:requestor_email, approver_uid =:requestor_uid, approver_role =:requestor_role, "+
        "approved_date = NOW() AT TIME ZONE 'UTC' + INTERVAL '9 hours' , update_type = 'domain_auto' " +
        "WHERE view_status = 'open'  AND channel = :channel  AND bpid = :bpid AND SUBSTRING(requestor_email FROM POSITION('@' IN requestor_email) + 1) = :email_domain  " ,  nativeQuery = true)
        void insert_Domain_CIAMAdmin(
            @Param("wf_id") String wf_id, 
            @Param("channel") String channel, 
            @Param("email_domain") String email_domain, 
            @Param("bpid") String bpid,
            @Param("bp_name") String bp_name,
            @Param("requestor_uid") String requestor_uid,
            @Param("requestor_email") String requestor_email,
            @Param("requestor_role") String requestor_role
    );

}