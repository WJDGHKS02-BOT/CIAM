package com.samsung.ciam.repositories;

import com.samsung.ciam.models.WfList;
import com.samsung.ciam.models.WfMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.query.Procedure;

import java.util.*;
import java.util.Optional;

public interface WfMasterRepository  extends JpaRepository<WfMaster, String> {
    // 현재 날짜에 해당하는 wf_id의 최대값을 가져오는 쿼리
    @Query("SELECT MAX(w.wfId) FROM WfMaster w WHERE w.wfId LIKE CONCAT(:prefix, '%')")
    String findMaxWfIdWithPrefix(String prefix);

    // W01 신규 가입승인 결재라인 생성
    @Procedure(name = "public.insert_wf_master_new_user")
    void insert_wf_master_new_user(
        @Param("p_wf_id") String wf_id, 
        @Param("p_channel") String channel, 
        @Param("p_country") String country, 
        @Param("p_subsidiary") String subsidiary,
        @Param("p_division") String division, 
        @Param("p_bpid") String bpid,
        @Param("p_bp_name") String bp_name,
        @Param("p_requestor_uid") String requestor_uid,
        @Param("p_requestor_email") String requestor_email,
        @Param("p_requestor_role") String requestor_role,
        @Param("p_workflow_code") String wf_code
    );
    //  결재라인 Search 
    @Query(value = "SELECT arm.*, ar.rule_level, ar.role " +
            "FROM public.approval_rule ar " +
            "JOIN public.approval_rule_master arm " +
            "ON ar.rule_master_id = arm.rule_master_id " +
            "WHERE arm.rule_master_id = 'Master' " +
            "UNION " +
            "SELECT arm.*, ar.rule_level, ar.role " +
            "FROM public.approval_rule ar " +
            "JOIN public.approval_rule_master arm " +
            "ON ar.rule_master_id = arm.rule_master_id " +
            "WHERE arm.channel = :channel " +
            "AND arm.workflow_code = :workflow_code " +
            "AND arm.country = COALESCE( " +
            "    (SELECT country FROM public.approval_rule_master " +
            "    WHERE channel = :channel " +
            "    AND workflow_code = :workflow_code " +
            "    AND country = :country " +
            "    AND ((subsidiary = :subsidiary AND division = :division) " +
            "        OR (subsidiary = 'ALL' AND division = :division) " +
            "        OR (subsidiary = :subsidiary AND division = 'ALL') " +
            "        OR (subsidiary = 'ALL' AND division = 'ALL')) " +
            "    ORDER BY CASE WHEN country = 'ALL' THEN 9999 ELSE 1 END ASC " +
            "    LIMIT 1), 'ALL') " +
            "AND arm.subsidiary = COALESCE( " +
            "    (SELECT subsidiary FROM public.approval_rule_master " +
            "    WHERE channel = :channel " +
            "    AND workflow_code = :workflow_code " +
            "    AND subsidiary = :subsidiary " +
            "    AND ((country = :country AND division = :division) " +
            "        OR (country = 'ALL' AND division = :division) " +
            "        OR (country = :country AND division = 'ALL') " +
            "        OR (country = 'ALL' AND division = 'ALL')) " +
            "    ORDER BY CASE WHEN subsidiary = 'ALL' THEN 9999 ELSE 1 END ASC " +
            "    LIMIT 1), 'ALL') " +
            "AND arm.division = COALESCE( " +
            "    (SELECT division FROM public.approval_rule_master " +
            "    WHERE channel = :channel " +
            "    AND workflow_code = :workflow_code " +
            "    AND division = :division " +
            "    AND ((country = :country AND subsidiary = :subsidiary) " +
            "        OR (country = 'ALL' AND subsidiary = :subsidiary) " +
            "        OR (country = :country AND subsidiary = 'ALL') " +
            "        OR (country = 'ALL' AND subsidiary = 'ALL')) " +
            "    ORDER BY CASE WHEN division = 'ALL' THEN 9999 ELSE 1 END ASC " +
            "    LIMIT 1), 'ALL') " +
            "AND ar.status = TRUE " +
            "ORDER BY rule_level;", nativeQuery = true)
    List<Map<String, Object>> searchRule(@Param("channel") String channel,
                                         @Param("workflow_code") String workflow_code,
                                         @Param("country") String country,
                                         @Param("subsidiary") String subsidiary,
                                         @Param("division") String division
    );



    //  결재라인 Search Partenr Admin
    @Query(value =      "WITH combined_data AS (" +
        "SELECT * FROM approval_admins WHERE type = 'PA' AND  active=true AND  channel = :channel " +
        "AND company_code = :company_code " +
        "UNION SELECT * FROM approval_admins WHERE  active=true AND  tempapprover = TRUE " +
        "AND tempapprovalrule = :rule_master_id " +
        "AND tempapprovalrulelevel = :rule_level), uids_to_exclude AS (" +
        "SELECT uid FROM combined_data WHERE  active=true AND  disabled = TRUE  " +
        "AND disabledrule = :rule_master_id AND disabledrulelevel = :rule_level " +
        "GROUP BY uid), ranked_data AS (" +
        "SELECT *, ROW_NUMBER() OVER (PARTITION BY uid ORDER BY id) AS rn FROM combined_data) " +
        "SELECT * FROM ranked_data WHERE rn = 1 " +
        "AND uid NOT IN (SELECT uid FROM uids_to_exclude);", nativeQuery = true)
        List<Map<String, Object>> searchPA(@Param("channel") String channel, 
                                        @Param("company_code") String company_code,
                                        @Param("rule_master_id") String rule_master_id,
                                        @Param("rule_level") String rule_level
                                        );

    //  결재라인 Search Channel Biz Admin
    @Query(value = "WITH combined_data AS (" +
            "SELECT * FROM approval_admins WHERE type = 'CB' " +
            "AND active = true AND channel = :channel " +
            "AND (COALESCE(NULLIF(TRIM(subsidiary), ''), 'ALL') ~ ('(^|,)' || :subsidiary || '(,|$)')) " +
            "AND (COALESCE(NULLIF(TRIM(country), ''), 'ALL') ~ ('(^|,)' || :country || '(,|$)')) " +
            "AND COALESCE(NULLIF(TRIM(division), ''), 'ALL') = :division " +
            "UNION " +
            "SELECT * FROM approval_admins WHERE active = true AND tempapprover = TRUE " +
            "AND tempapprovalrule = :rule_master_id AND tempapprovalrulelevel = :rule_level), " +
            "uids_to_exclude AS (" +
            "SELECT uid FROM combined_data WHERE active = true AND disabled = TRUE " +
            "AND disabledrule = :rule_master_id AND disabledrulelevel = :rule_level " +
            "GROUP BY uid), " +
            "ranked_data AS (" +
            "SELECT *, ROW_NUMBER() OVER (PARTITION BY uid ORDER BY id) AS rn FROM combined_data) " +
            "SELECT * FROM ranked_data WHERE rn = 1 " +
            "AND uid NOT IN (SELECT uid FROM uids_to_exclude);",
            nativeQuery = true)
        List<Map<String, Object>> searchCB(@Param("channel") String channel, 
                                        @Param("country") String country, 
                                        @Param("subsidiary") String subsidiary, 
                                        @Param("division") String division, 
                                        @Param("rule_master_id") String rule_master_id, 
                                        @Param("rule_level") String rule_level);
            

    //  결재라인 Search Channel Admin
    @Query(value =  "WITH combined_data AS (" +
        "SELECT * FROM approval_admins WHERE active=true AND  type = 'CA' AND channel = :channel " +
        "UNION " +
        "SELECT * FROM approval_admins WHERE active=true AND  tempapprover = TRUE " +
        "AND tempapprovalrule = :rule_master_id AND tempapprovalrulelevel = :rule_level), " +
        "uids_to_exclude AS (" +
        "SELECT uid FROM combined_data WHERE active=true AND  disabled = TRUE " +
        "AND disabledrule = :rule_master_id AND disabledrulelevel = :rule_level " +
        "GROUP BY uid), " +
        "ranked_data AS (" +
        "SELECT *, ROW_NUMBER() OVER (PARTITION BY uid ORDER BY id) AS rn FROM combined_data) " +
        "SELECT * FROM ranked_data WHERE rn = 1 " +
        "AND uid NOT IN (SELECT uid FROM uids_to_exclude);", 
        nativeQuery = true)
        List<Map<String, Object>> searchCA(@Param("channel") String channel, 
                        @Param("rule_master_id") String rule_master_id,
                        @Param("rule_level") String rule_level);

        //  결재라인 Search Channel Admin
        @Query(value =  " SELECT * FROM approval_admins where active=true AND  type='AM' ", nativeQuery = true)
        List<Map<String, Object>> searchAM();

        // W07 insertDomain 
        @Transactional
        @Modifying
        @Query(value = "INSERT INTO verification_domains (wf_id, channel, email_domain, status, bpid, bp_name, requestor_uid, requestor_email, requestor_role, request_date ) " +
        "VALUES " +
        "(:wf_id, :channel, :email_domain, 'pending', :bpid, :bp_name, :requestor_uid, :requestor_email, :requestor_role, " +
        "NOW() AT TIME ZONE 'UTC' + INTERVAL '9 hours' );" , nativeQuery = true)
        void insertDomain(
                @Param("wf_id") String wf_id, 
                @Param("channel") String channel, 
                @Param("email_domain") String email_domain, 
                @Param("bpid") String bpid,
                @Param("bp_name") String bp_name,
                @Param("requestor_uid") String requestor_uid,
                @Param("requestor_email") String requestor_email,
                @Param("requestor_role") String requestor_role
        );

        // W05 verification_domains Insert
        @Modifying
        @Transactional
        @Query(value = "INSERT INTO role_management (  wf_id, channel, change_role, status, bpid, bp_name, requestor_uid, requestor_email, previous_role, request_date) " +
        "VALUES " +
        "(:wf_id, :channel, :change_role, :status, :bpid, :bp_name, :requestor_uid, :requestor_email, :requestor_role, " +
        "NOW() AT TIME ZONE 'UTC' + INTERVAL '9 hours' );" , nativeQuery = true)
        int insertRoleManagement(       @Param("wf_id") String wf_id, 
                                @Param("channel") String cdc_email, 
                                @Param("change_role") String change_role,  
                                @Param("status") String status,  
                                @Param("bpid") String bpid,
                                @Param("bp_name") String bp_name,
                                @Param("requestor_uid") String requestor_uid,
                                @Param("requestor_email") String requestor_email,
                                @Param("requestor_role") String requestor_role
                        );
        
        // wf_master Insert 
        @Transactional
        @Modifying
        @Query(value =  "INSERT INTO wf_master ( wf_id, channel, workflow_code, requestor_id, requestor_email, requestor_role, status," + 
        " requested_date, wf_max_level, requestor_company_name, requestor_company_code, rule_master_id, approved_date, approver_id, approver_email, approved_level ) " +
        " VALUES ( " +
        " :wf_id, :channel, :workflow_code, :requestor_uid, :requestor_email, :requestor_role, :status, " +
        " NOW() AT TIME ZONE 'UTC' + INTERVAL '9 hours', :max_wf_level, :requestor_company_name, :requestor_company_code, :rule_master_id, " +
        " CASE WHEN :status = 'approved' THEN NOW() AT TIME ZONE 'UTC' + INTERVAL '9 hours' ELSE NULL END, " + // approved_date
        " CASE WHEN :status = 'approved' THEN 'system' ELSE NULL END, " + // approved_id
        " CASE WHEN :status = 'approved' THEN 'system@system' ELSE NULL END, " + // approved_email
        " CASE WHEN :status = 'approved' THEN :max_wf_level ELSE NULL END  " + 
        ");", nativeQuery = true)
        void insertWFMaster(
                @Param("wf_id") String wf_id, 
                @Param("channel") String channel, 
                @Param("workflow_code") String workflow_code,  
                @Param("requestor_uid") String requestor_uid, 
                @Param("requestor_email") String requestor_email, 
                @Param("requestor_role") String requestor_role, 
                @Param("requestor_company_code") String requestor_company_code, 
                @Param("requestor_company_name") String requestor_company_name, 
                @Param("status") String status, 
                @Param("rule_master_id") String rule_master_id, 
                @Param("max_wf_level") int max_wf_level);

        // wf_master Insert W03 초대, invitaion_sender_id, invitaion_sender_email
 
        @Transactional
        @Modifying
        @Query(value =  "INSERT INTO wf_master ( wf_id, channel, workflow_code, requestor_id, requestor_email, requestor_role, status," + 
        " requested_date, wf_max_level, requestor_company_name, requestor_company_code, rule_master_id, approved_date, approver_id, approver_email, approved_level, invitaion_sender_id, invitaion_sender_email ) " +
        " VALUES ( " +
        " :wf_id, :channel, :workflow_code, :requestor_uid, :requestor_email, :requestor_role, :status, " +
        " NOW() AT TIME ZONE 'UTC' + INTERVAL '9 hours', :max_wf_level, :requestor_company_name, :requestor_company_code, :rule_master_id, " +
        " CASE WHEN :status = 'approved' THEN NOW() AT TIME ZONE 'UTC' + INTERVAL '9 hours' ELSE NULL END, " + // approved_date
        " CASE WHEN :status = 'approved' THEN 'system' ELSE NULL END, " + // approved_id
        " CASE WHEN :status = 'approved' THEN 'system@system' ELSE NULL END, " + // approved_email
        " CASE WHEN :status = 'approved' THEN :max_wf_level ELSE NULL END,  " + 
        " :invitaion_sender_id, :invitaion_sender_email  );", nativeQuery = true)
        void insertWFMasterW03(
                @Param("wf_id") String wf_id, 
                @Param("channel") String channel, 
                @Param("workflow_code") String workflow_code,  
                @Param("requestor_uid") String requestor_uid, 
                @Param("requestor_email") String requestor_email, 
                @Param("requestor_role") String requestor_role, 
                @Param("requestor_company_code") String requestor_company_code, 
                @Param("requestor_company_name") String requestor_company_name, 
                @Param("status") String status, 
                @Param("rule_master_id") String rule_master_id, 
                @Param("max_wf_level") int max_wf_level,
                @Param("invitaion_sender_id") String invitaion_sender_id,
                @Param("invitaion_sender_email") String invitaion_sender_email
                );


        // wf_master Insert W06
        @Transactional
        @Modifying
        @Query(value =  "INSERT INTO wf_master ( wf_id, channel, workflow_code, requestor_id, requestor_email, requestor_role, status," + 
        " requested_date, wf_max_level, requestor_company_name, requestor_company_code, rule_master_id, approved_date, approver_id, approver_email, approved_level, reg_channel, target_channel, target_channeltype  ) " +
        " VALUES ( " +
        " :wf_id, :channel, :workflow_code, :requestor_uid, :requestor_email, :requestor_role, :status, " +
        " NOW() AT TIME ZONE 'UTC' + INTERVAL '9 hours', :max_wf_level, :requestor_company_name, :requestor_company_code, :rule_master_id, " +
        " CASE WHEN :status = 'approved' THEN NOW() AT TIME ZONE 'UTC' + INTERVAL '9 hours' ELSE NULL END, " + // approved_date
        " CASE WHEN :status = 'approved' THEN 'system' ELSE NULL END, " + // approved_id
        " CASE WHEN :status = 'approved' THEN 'system@system' ELSE NULL END, " + // approved_email
        " CASE WHEN :status = 'approved' THEN :max_wf_level ELSE NULL END, :reg_channel, :target_channel, :target_channeltype " + 
        ");", nativeQuery = true)
        void insertWFMaster(
                @Param("wf_id") String wf_id, 
                @Param("channel") String channel, 
                @Param("workflow_code") String workflow_code,  
                @Param("requestor_uid") String requestor_uid, 
                @Param("requestor_email") String requestor_email, 
                @Param("requestor_role") String requestor_role, 
                @Param("requestor_company_code") String requestor_company_code, 
                @Param("requestor_company_name") String requestor_company_name, 
                @Param("status") String status, 
                @Param("rule_master_id") String rule_master_id, 
                @Param("max_wf_level") int max_wf_level,
                @Param("reg_channel") String reg_channel, 
                @Param("target_channel") String target_channel, 
                @Param("target_channeltype") String target_channeltype )   ;

        // wf_List Insert 
        @Transactional
        @Modifying
        @Query(value =  "INSERT INTO wf_list ( wf_id, workflow_code, wf_level, status, approval_format, approver_id, approver_email, approver_role, approved_date, approved_id, approved_email ) " +
        "VALUES ( " +
        " :wf_id, :workflow_code, :wf_level, :status, :approval_format, :approver_id, :approver_email, :approver_role, " +
        " CASE WHEN :status = 'approved' THEN NOW() AT TIME ZONE 'UTC' + INTERVAL '9 hours' ELSE NULL END, " + // approved_date
        " CASE WHEN :status = 'approved' THEN :approver_id ELSE NULL END, " + // approved_id
        " CASE WHEN :status = 'approved' THEN :approver_email ELSE NULL END " // approved_email
        + ");", nativeQuery = true)
        void insertWFList(
                @Param("wf_id") String wf_id, 
                @Param("workflow_code") String workflow_code,  
                @Param("wf_level") int wf_level, 
                @Param("status") String status, 
                @Param("approval_format") String approval_format, 
                @Param("approver_id") String approver_id, 
                @Param("approver_email") String approver_email, 
                @Param("approver_role") String approver_role);



    //  W01  신규 가입승인 list Search
    @Query(value = "SELECT wl.id, wl.wf_id, wl.workflow_code, wl.status, wm.channel, wm.requestor_id, wm.requestor_email, wm.requestor_company_name, wm.requestor_company_code, " +
                " wm.wf_max_level, wm.approved_level, wm.reg_channel, wm.target_channel,  wm.target_channeltype , wm.invitaion_sender_id, wm.invitaion_sender_email,  "+
                " wl.wf_level, wm.requestor_role, wm.reject_reason,  wl.approved_id , wl.approved_email , wl.approval_format ,  "+
               "TO_CHAR(wm.requested_date, 'YYYY-MM-DD') AS requested_date, " +
               "TO_CHAR(wl.approved_date, 'YYYY-MM-DD') AS approved_date, " +
               "TO_CHAR(wm.rejected_date, 'YYYY-MM-DD') AS rejected_date, " +
               "CASE " +
               "    WHEN wm.workflow_code = 'W01' THEN 'New(신규)' " +
               "    WHEN wm.workflow_code = 'W02' THEN 'Conversion(전환)' " +
               "    WHEN wm.workflow_code = 'W03' THEN 'Invite(초대)' " +
               "    WHEN wm.workflow_code = 'W04' THEN 'AD 가입' " +
               "    WHEN wm.workflow_code = 'W06' THEN 'SSO Access' " +
               "    ELSE 'Unknown' " +
               "END AS registration_type " +
               "FROM wf_list wl " +
               "JOIN wf_master wm ON wl.wf_id = wm.wf_id " +
               " WHERE wl.workflow_code = :workflow_code " +
               //" AND wl.status = :status " +
               " AND ( CASE  WHEN :status = 'inprogress' THEN wl.status = 'approved'  ELSE wl.status =  :status  END ) " +
               " AND wl.approver_id = :uid " +
               //"AND wl.wf_level = COALESCE(wm.approved_level, 0) + 1 " +
               " AND   ( CASE  WHEN :status = 'pending' THEN wl.wf_level = COALESCE(wm.approved_level, 0) + 1  ELSE wl.wf_level = COALESCE(wm.approved_level, 0)  END ) "+
               " AND wm.status in (:status ,'inprogress' ) " +
               " And ( CASE WHEN :workflow_code = 'W06' THEN wm.target_channel = :channel ELSE wm.channel = :channel END ) ",
       nativeQuery = true)
     List<Map<String, Object>> searchW01(@Param("uid") String uid, @Param("status") String status, @Param("workflow_code") String workflow_code, @Param("channel") String channel);

    //  W01  신규 가입승인 list Search 'Approved 일때!!!'
    @Query(value = "SELECT wl.id, wl.wf_id, wl.workflow_code, wl.status, wm.channel, wm.requestor_id, wm.requestor_email, wm.requestor_company_name, wm.requestor_company_code,  wl.approval_format ," +
                " wm.wf_max_level, wm.approved_level, wm.reg_channel, wm.target_channel,  wm.target_channeltype ,  wm.invitaion_sender_id, wm.invitaion_sender_email, wl.wf_level, wm.requestor_role, wm.reject_reason,  wl.approved_id , wl.approved_email ,  "+
               "TO_CHAR(wm.requested_date, 'YYYY-MM-DD') AS requested_date, " +
               "TO_CHAR(wl.approved_date, 'YYYY-MM-DD') AS approved_date, " +
               "TO_CHAR(wm.rejected_date, 'YYYY-MM-DD') AS rejected_date, " +
               "CASE " +
               "    WHEN wm.workflow_code = 'W01' THEN 'New(신규)' " +
               "    WHEN wm.workflow_code = 'W02' THEN 'Conversion(전환)' " +
               "    WHEN wm.workflow_code = 'W03' THEN 'Invite(초대)' " +
               "    WHEN wm.workflow_code = 'W04' THEN 'AD 가입' " +
               "    WHEN wm.workflow_code = 'W06' THEN 'SSO Access' " +
               "    ELSE 'Unknown' " +
               "END AS registration_type " +
               "FROM wf_list wl " +
               "JOIN wf_master wm ON wl.wf_id = wm.wf_id " +
               " WHERE wl.workflow_code = :workflow_code " +
               " AND wl.status =  :status   " +
               " AND wl.approver_id = :uid " +
               " AND wm.status = :status  " +
               " And ( CASE WHEN :workflow_code = 'W06' THEN wm.target_channel = :channel ELSE wm.channel = :channel END ) ",
       nativeQuery = true)
     List<Map<String, Object>> searchW01Appr(@Param("uid") String uid, @Param("status") String status, @Param("workflow_code") String workflow_code, @Param("channel") String channel);



    //  W05 Role list Search
    @Query(value = "SELECT rm.id, rm.wf_id, rm.bpid, rm.bp_name, rm.change_role, rm.requestor_uid, rm.requestor_email, rm.channel, " +
               "rm.previous_role, rm.approver_id, rm.approver_email, rm.approver_role, rm.reject_reason, wl.approval_format , " +
               "TO_CHAR(rm.request_date, 'YYYY-MM-DD') AS request_date, " +
               "TO_CHAR(rm.approved_date, 'YYYY-MM-DD') AS approved_date, " +
               "TO_CHAR(rm.rejected_date, 'YYYY-MM-DD') AS rejected_date " +
               " FROM wf_list wl JOIN role_management rm ON wl.wf_id = rm.wf_id " +
               " WHERE wl.workflow_code = 'W05' " +
               " AND wl.status = :status " +
               " AND wl.approver_id = :uid " +
               " AND rm.status = :status " +
               " AND rm.channel = :channel ",
       nativeQuery = true)
     List<Map<String, Object>> rolePending(@Param("uid") String uid, @Param("status") String status, @Param("channel") String channel);


    //  W07 domain pending list Search
    @Query(value = "SELECT vd.id, vd.wf_id, vd.bpid, vd.bp_name, vd.email_domain, vd.requestor_uid, vd.requestor_email, " +
               "vd.requestor_role, vd.approver_id, vd.approver_email, vd.approver_role, vd.reject_reason, wl.approval_format , " +
               "TO_CHAR(vd.request_date, 'YYYY-MM-DD') AS request_date, " +
               "TO_CHAR(vd.approved_date, 'YYYY-MM-DD') AS approved_date, " +
               "TO_CHAR(vd.rejected_date, 'YYYY-MM-DD') AS rejected_date, " +
               "(" +
               "    SELECT STRING_AGG(sub_vd.email_domain, ', ') " +
               "    FROM verification_domains sub_vd " +
               "    WHERE sub_vd.bpid = vd.bpid " +
               "      AND sub_vd.channel = vd.channel " +
               "      AND sub_vd.status = 'approved' " +
               ") AS company_domain " +
               " FROM wf_list wl JOIN verification_domains vd ON wl.wf_id = vd.wf_id " +
               " WHERE wl.workflow_code = 'W07' " +
               " AND wl.status = :status " +
               " AND wl.approver_id = :uid " +
               " AND vd.status = :status " +
               " AND vd.channel = :channel ",
       nativeQuery = true)
     List<Map<String, Object>> domainPending(@Param("uid") String uid, @Param("status") String status, @Param("channel") String channel);

    //  W01 신규가입승인 wf_list 상태 업데이트
    @Modifying
    @Transactional
    @Query(value = "UPDATE wf_list " +
                   "SET status = :status,  approved_id = :uid,  approved_email = :cdc_email, " +
                   "approved_date = CASE WHEN :status = 'approved' THEN NOW() AT TIME ZONE 'UTC' + INTERVAL '9 hours' ELSE approved_date END, " +
                   "rejected_date = CASE WHEN :status = 'rejected' THEN NOW() AT TIME ZONE 'UTC' + INTERVAL '9 hours' ELSE rejected_date END " +
                   "WHERE wf_id = :wf_id and wf_level = :wf_level", 
           nativeQuery = true)
    int updateW01WfList(@Param("wf_id") String wf_id, @Param("status") String status, @Param("uid") String uid, 
                        @Param("cdc_email") String cdc_email, @Param("wf_level") Integer wf_level  );

    //   W01 신규가입승인 wf_master 상태 업데이트
    @Modifying
    @Transactional
    @Query(value = "UPDATE wf_master " +
                "SET approver_id = :uid, status = :status, " +
                "approver_email = :cdc_email, requestor_role = :requestor_role, " +
                "approved_level = CASE WHEN :status = 'approved' OR :status = 'inprogress' OR :status = 'rejected' THEN :wf_level ELSE approved_level END,  " + 
                "reject_reason = :reject_reason, " +
                "approved_date = CASE WHEN :status = 'approved' THEN NOW() AT TIME ZONE 'UTC' + INTERVAL '9 hours' ELSE approved_date END, " + 
                "rejected_date = CASE WHEN :status = 'rejected' THEN NOW() AT TIME ZONE 'UTC' + INTERVAL '9 hours' ELSE rejected_date END " +
                "WHERE wf_id = :wf_id", 
        nativeQuery = true)
    int updateW01WfMaster   (@Param("uid") String uid, 
                            @Param("wf_id") String wf_id, 
                            @Param("cdc_email") String cdc_email, 
                            @Param("requestor_role") String requestor_role, 
                            @Param("status") String status,  
                            @Param("reject_reason") String reject_reason, 
                            @Param("wf_level") Integer wf_level );


    //  W07 domain wf_list 상태 업데이트
    @Modifying
    @Transactional
    @Query(value = "UPDATE wf_list " +
                   "SET status = :status, " +
                   "approved_date = CASE WHEN :status = 'approved' THEN NOW() AT TIME ZONE 'UTC' + INTERVAL '9 hours' ELSE approved_date END, " +
                   "rejected_date = CASE WHEN :status = 'rejected' THEN NOW() AT TIME ZONE 'UTC' + INTERVAL '9 hours' ELSE rejected_date END " +
                   "WHERE wf_id = :wf_id", 
           nativeQuery = true)
    int updateWfListStatus(@Param("wf_id") String wf_id, @Param("status") String status);
    // W07 domain wf_master 상태 업데이트
    @Modifying
    @Transactional
    @Query(value = "UPDATE wf_master " +
                "SET approver_id = :uid, " +
                "status = :status, " +
                "approver_email = :cdc_email, " +
                "approved_level = 1, " +
                "reject_reason = :reject_reason, " +
                "approved_date = CASE WHEN :status = 'approved' THEN NOW() AT TIME ZONE 'UTC' + INTERVAL '9 hours' ELSE approved_date END, " + // 쉼표 추가
                "rejected_date = CASE WHEN :status = 'rejected' THEN NOW() AT TIME ZONE 'UTC' + INTERVAL '9 hours' ELSE rejected_date END " +
                "WHERE wf_id = :wf_id", 
        nativeQuery = true)
    int updateWfMasterStatus(@Param("uid") String uid, 
                            @Param("wf_id") String wf_id, 
                            @Param("cdc_email") String cdc_email, 
                            @Param("status") String status,  
                            @Param("reject_reason") String reject_reason);

    // W07 verification_domains 상태 업데이트
    @Modifying
    @Transactional
    @Query(value = "UPDATE verification_domains " +
                "SET approver_id = :uid, " +
                "status = :status, " +
                "approver_email = :cdc_email, " +
                "reject_reason = :reject_reason, " +
                "approved_date = CASE WHEN :status = 'approved' THEN NOW() AT TIME ZONE 'UTC' + INTERVAL '9 hours' ELSE approved_date END, " + // 쉼표 추가
                "rejected_date = CASE WHEN :status = 'rejected' THEN NOW() AT TIME ZONE 'UTC' + INTERVAL '9 hours' ELSE rejected_date END " +
                "WHERE wf_id = :wf_id", 
        nativeQuery = true)
    int updateVerificationDomains(@Param("uid") String uid, 
                                @Param("wf_id") String wf_id, 
                                @Param("cdc_email") String cdc_email, 
                                @Param("status") String status,  
                                @Param("reject_reason") String reject_reason);


    // W07 verification_employ 상태 업데이트 kimjy 2024.09.11 -- working
    @Modifying
    @Transactional
    @Query(value = "update  verification_employ ve SET user_status = 'approved', approver_email =:cdc_email, approver_uid =:uid, " +
                " approved_date = NOW() AT TIME ZONE 'UTC' + INTERVAL '9 hours' , update_type = 'domain_self' " +
                " FROM verification_domains vd WHERE ve.view_status = 'open' AND vd.wf_id = :wf_id  " +
                " AND ve.channel = vd.channel   AND ve.bpid = vd.bpid  " +
                " AND SUBSTRING(ve.requestor_email FROM POSITION('@' IN ve.requestor_email) + 1) = vd.email_domain ; " , 
        nativeQuery = true)
    int updateVerificationEmploy(@Param("uid") String uid, //승인자 UID 
                                @Param("wf_id") String wf_id,  
                                @Param("cdc_email") String cdc_email );


    // W05 role_management 상태 업데이트
    @Modifying
    @Transactional
    @Query(value = "UPDATE role_management " +
                "SET approver_id = :uid, " +
                "status = :status, " +
                "approver_email = :cdc_email, " +
                "reject_reason = :reject_reason, " +
                "approved_date = CASE WHEN :status = 'approved' THEN NOW() AT TIME ZONE 'UTC' + INTERVAL '9 hours' ELSE approved_date END, " + // 쉼표 추가
                "rejected_date = CASE WHEN :status = 'rejected' THEN NOW() AT TIME ZONE 'UTC' + INTERVAL '9 hours' ELSE rejected_date END " +
                "WHERE wf_id = :wf_id", 
        nativeQuery = true)
    int updateRoleManagement(@Param("uid") String uid, 
                                @Param("wf_id") String wf_id, 
                                @Param("cdc_email") String cdc_email, 
                                @Param("status") String status,  
                                @Param("reject_reason") String reject_reason);

    @Query(value = "SELECT * FROM wf_master wm WHERE wm.wf_id = :wfId LIMIT 1", nativeQuery = true)
    WfMaster selectWfMaster(@Param("wfId") String wfId);

    // companyCode,CompanyName업데이트
    @Modifying
    @Transactional
    @Query(value = "UPDATE wf_master " +
            "SET requestor_company_code = :accountId ," +
            "requestor_company_name = :name " +
            "WHERE wf_id = :wf_id",
            nativeQuery = true)
    int updateWfMasterCompanyCode(@Param("wf_id") String wf_id,@Param("accountId") String accountId,@Param("name") String name);

}