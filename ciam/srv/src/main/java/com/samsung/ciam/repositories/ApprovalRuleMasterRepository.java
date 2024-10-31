package com.samsung.ciam.repositories;

import com.samsung.ciam.models.ApprovalRuleMaster;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ApprovalRuleMasterRepository extends JpaRepository<ApprovalRuleMaster, String>{

    @Query(value = "SELECT rule_master_id FROM approval_rule_master " +
                   " WHERE workflow_code = :workflowCode and channel = :channel and UPPER(country) = UPPER(:country) and UPPER(subsidiary) = UPPER(:subsidiary) and UPPER(division) = UPPER(:division)", nativeQuery = true)
String selectApprovalRuleMasterId(@Param("workflowCode") String workflowCode, @Param("channel") String channel, @Param("country") String country, @Param("subsidiary") String subsidiary, @Param("division") String division);
    
    @Query(value = "SELECT count(rule_master_id) FROM approval_rule_master " +
                   " WHERE workflow_code = :workflowCode and channel = :channel and UPPER(country) = UPPER(:country) and UPPER(subsidiary) = UPPER(:subsidiary) and UPPER(division) = UPPER(:division)", nativeQuery = true)
    int selectApprovalRuleMasterCount(@Param("workflowCode") String workflowCode, @Param("channel") String channel, @Param("country") String country, @Param("subsidiary") String subsidiary, @Param("division") String division);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO public.approval_rule_master(rule_master_id, workflow_code, channel, country, subsidiary, division, created_at, stage)" +
                   "VALUES(TO_CHAR(NOW() AT TIME ZONE 'UTC' + INTERVAL '9 hours', 'YYYYMMDDHH24MISS') || '_' || :workflowCode, :workflowCode, :channel, :country, :subsidiary, :division, NOW() AT TIME ZONE 'UTC' + INTERVAL '9 hours', 0)", nativeQuery = true)
    Integer insertApprovalRuleMaster(@Param("workflowCode") String workflowCode, @Param("channel") String channel, @Param("country") String country, @Param("subsidiary") String subsidiary, @Param("division") String division);

    @Modifying
    @Transactional
    @Query(value = "INSERT INTO public.approval_rule_master (rule_master_id, workflow_code, channel, country, subsidiary, division, created_at, stage) " +
                   "VALUES(:ruleMasterId, :workflowCode, :channel, :country, :subsidiary, :division, NOW() AT TIME ZONE 'UTC' + INTERVAL '9 hours', :stage) " +
                   "ON CONFLICT (rule_master_id) " + 
                   "DO UPDATE " + 
                   "SET workflow_code=:workflowCode, channel=:channel, country=:country, subsidiary=:subsidiary, division=:division, updated_at=(NOW() AT TIME ZONE 'UTC' + INTERVAL '9 hours'), stage=:stage, approve_format=:approveFormat " +
                   "WHERE public.approval_rule_master.rule_master_id = :ruleMasterId", nativeQuery = true)
    Integer saveApprovalRuleMasterStage(@Param("ruleMasterId") String ruleMasterId, @Param("workflowCode") String workflowCode, @Param("channel") String channel, @Param("country") String country, @Param("subsidiary") String subsidiary, @Param("division") String division, @Param("stage") int stage, @Param("approveFormat") String approveFormat);

    @Query(value = "SELECT COALESCE((SELECT rule_master_id FROM public.approval_rule_master WHERE workflow_code=:workflowCode AND channel=:channel AND UPPER(country)=UPPER(:country) AND UPPER(subsidiary)=UPPER(:subsidiary) AND UPPER(division)=UPPER(:division) limit 1), (TO_CHAR(NOW() AT TIME ZONE 'UTC' + INTERVAL '9 hours', 'YYYYMMDDHH24MISS') || '_' || :workflowCode))", nativeQuery = true)
    String getApprovalRuleMasterId(@Param("workflowCode") String workflowCode, @Param("channel") String channel, @Param("country") String country, @Param("subsidiary") String subsidiary, @Param("division") String division);

    @Query(value = "SELECT COALESCE((SELECT stage FROM public.approval_rule_master WHERE workflow_code=:workflowCode AND channel=:channel AND UPPER(country)=UPPER(:country) AND UPPER(subsidiary)=UPPER(:subsidiary) AND UPPER(division)=UPPER(:division) limit 1), 1)", nativeQuery = true)
    Integer getApprovalRuleMasterStage(@Param("workflowCode") String workflowCode, @Param("channel") String channel, @Param("country") String country, @Param("subsidiary") String subsidiary, @Param("division") String division);

    @Query(value = "SELECT COALESCE((SELECT approve_format as approveFormat FROM public.approval_rule_master WHERE workflow_code=:workflowCode AND channel=:channel AND UPPER(country)=UPPER(:country) AND UPPER(subsidiary)=UPPER(:subsidiary) AND UPPER(division)=UPPER(:division) limit 1), 'self')", nativeQuery = true)
    String getApprovalRuleMasterApproveFormat(@Param("workflowCode") String workflowCode, @Param("channel") String channel, @Param("country") String country, @Param("subsidiary") String subsidiary, @Param("division") String division);

    @Query(value = "SELECT gs AS ruleLevel FROM generate_series(1, 4) as gs WHERE gs NOT IN (SELECT ar.rule_level::int from approval_rule ar inner join approval_rule_master arm on ar.rule_master_id = arm.rule_master_id WHERE " +
                   " status = true and (arm.workflow_code=:workflowCode AND arm.channel=:channel AND UPPER(arm.country)=UPPER(:country) AND UPPER(arm.subsidiary)=UPPER(:subsidiary) AND UPPER(arm.division)=UPPER(:division) and rule_level not in ('Master','')) )", nativeQuery = true)
    List<Integer> getPossibleApprovalRuleList(@Param("workflowCode") String workflowCode, @Param("channel") String channel, @Param("country") String country, @Param("subsidiary") String subsidiary, @Param("division") String division);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM approval_rule_master WHERE rule_master_id = :ruleMasterId", nativeQuery = true)
    int deleteDataApprovalRuleMaster(@Param("ruleMasterId") String ruleMasterId);

    @Query(value = "SELECT * FROM public.approval_rule_master WHERE rule_master_id = :ruleMasterId", nativeQuery = true)
    List<ApprovalRuleMaster> selectApprovalRuleMasterList(@Param("ruleMasterId") String ruleMasterId);
}