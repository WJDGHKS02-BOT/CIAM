package com.samsung.ciam.repositories;

import com.samsung.ciam.models.ApprovalRule;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ApprovalRuleRepository extends JpaRepository<ApprovalRule, Long>{

    @Query(value = "SELECT ar.id, ar.rule_level, arm.workflow_code, (select name from common_code where header = 'REQUEST_TYPE_CODE' and code = arm.workflow_code) workflow_code_nm,  " + 
                   "(select name from common_code where header = 'ROLE_CODE' and attribute3 = ar.role) role_nm, arm.approve_format, " +
                   "(select name from common_code where header = 'APPROVE_FORMAT_CODE' and value = arm.approve_format) approve_format_nm , ar.approve_condition, " +
                   "(select name from common_code where header = 'APPROVE_CONDITION_CODE' and value = ar.approve_condition) approve_condition_nm, arm.division, (select name from common_code where header = 'DIVISION_CODE' and value = arm.division) division_nm,  " +
                   "TO_CHAR(COALESCE(ar.updated_at, ar.created_at), 'YYYY-MM-DD') AS last_modified_date, status,  role,  " +
                   "ar.created_at, TO_CHAR(ar.created_at, 'YYYY-MM-DD') created_date, ar.updated_at, TO_CHAR(ar.updated_at, 'YYYY-MM-DD') updated_date, arm.channel, arm.country, arm.subsidiary, arm.rule_master_id " +
                   "FROM approval_rule ar inner join approval_rule_master arm on ar.rule_master_id = arm.rule_master_id WHERE status = true and " +
                   "(('' = :channel or arm.channel = :channel) and ('' = :requestType or arm.workflow_code = :requestType) and UPPER(arm.country) = UPPER(:country) and UPPER(arm.subsidiary) = UPPER(:subsidiary) and UPPER(arm.division) = UPPER(:division) and rule_level not in ('Master','')) or ar.rule_level = 'Master' ORDER BY ar.rule_level", nativeQuery = true)
    List<ApprovalRule> selectApprovalRuleList(@Param("channel") String channel, @Param("requestType") String requestType, @Param("country") String country, @Param("subsidiary") String subsidiary, @Param("division") String division);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO public.approval_rule (id, rule_master_id, rule_level, approve_format, approve_condition, status, role, created_at) " +
                   " VALUES(nextval('approval_rule_id_seq'::regclass), :ruleMasterId, :ruleLevel, 'self', :approveCondition, true, :role, (NOW() AT TIME ZONE 'UTC' + INTERVAL '9 hours'))", nativeQuery = true)
    Integer insertApprovalRule(@Param("ruleMasterId") String ruleMasterId, @Param("ruleLevel") String ruleLevel,  @Param("approveCondition") String approveCondition, @Param("role") String role);
        
    @Modifying
    @Transactional
    @Query(value = "UPDATE public.approval_rule "+
                   "   SET rule_level=:ruleLevel, role=:role, approve_format='self', updated_at=(NOW() AT TIME ZONE 'UTC' + INTERVAL '9 hours') " +
                   " WHERE id=:id", nativeQuery = true)
    int updateApprovalRule(@Param("ruleLevel") String ruleLevel, @Param("role") String role, @Param("id") int id);
                
    @Modifying
    @Transactional
    @Query(value = "UPDATE approval_rule SET status = false WHERE id = :id", nativeQuery = true)
    int deleteApprovalRule(@Param("id") int id);

    @Query(value = "SELECT id, rule_level, (select name from common_code where header = 'REQUEST_TYPE_CODE' and code = a.workflow_code) workflow_code_nm, execution_condition, (select name from common_code where header = 'EXECUTION_CONDITION_CODE' and value = a.execution_condition) execution_condition_nm, (select name from common_code where header = 'ROLE_CODE' and attribute3 = a.role) role_nm, division, (select name from common_code where header = 'DIVISION_CODE' and value = a.division) division_nm, approve_format, (select name from common_code where header = 'APPROVE_FORMAT_CODE' and value = a.approve_format) approve_format_nm " + 
                   ", approve_condition, (select name from common_code where header = 'APPROVE_CONDITION_CODE' and value = a.approve_condition) approve_condition_nm, TO_CHAR(COALESCE(updated_at, created_at), 'YYYY-MM-DD') AS last_modified_date, status,  " +
                   "role, created_at, TO_CHAR(created_at, 'YYYY-MM-DD') created_date, updated_at, TO_CHAR(updated_at, 'YYYY-MM-DD') updated_date, channel, country, subsidiary, workflow_code " +
                   " FROM approval_rule a WHERE id = :id", nativeQuery = true)
    ApprovalRule selectApprovalRuleOne(@Param("id") int id);

    @Query(value = "SELECT count(*) FROM approval_rule WHERE status = true and rule_master_id = (SELECT rule_master_id FROM approval_rule WHERE id = :id)", nativeQuery = true)
    int selectApprovalRuleCnt(@Param("id") int id);

    @Query(value = "SELECT COALESCE(NULLIF(TRIM(rule_master_id), ''), '') ruleMasterId FROM approval_rule WHERE id = :id", nativeQuery = true)
    String selectApprovalRuleMasterId(@Param("id") int id);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM approval_rule WHERE rule_master_id = :ruleMasterId", nativeQuery = true)
    int deleteDataApprovalRule(@Param("ruleMasterId") String ruleMasterId);
}