package com.samsung.ciam.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "approval_rule")
public class ApprovalRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "channel", length = 50, nullable = false)
    private String channel;

    @Column(name = "workflow_code", length = 20)
    private String workflowCode;

    @Column(name = "workflow_code_nm", length = 20)
    private String workflowCodeNm;

    @Column(name = "country", length = 50)
    private String country;

    @Column(name = "subsidiary", length = 50)
    private String subsidiary;

    @Column(name = "division", length = 20)
    private String division;

    @Column(name = "division_nm", length = 50)
    private String divisionNm;

    @Column(name = "rule_level", length = 20)
    private String ruleLevel;

    @Column(name = "approve_format", length = 20)
    private String approveFormat;

    @Column(name = "approve_format_nm", length = 20)
    private String approveFormatNm;

    @Column(name = "approve_condition", length = 20)
    private String approveCondition;

    @Column(name = "approve_condition_nm", length = 20)
    private String approveConditionNm;

    @Column(name = "status")
    private Boolean status;

    @Column(name = "role", length = 20)
    private String role;

    @Column(name = "role_nm", length = 20)
    private String roleNm;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_date")
    private String createdDate;

    @Column(name = "updated_date")
    private String updatedDate;

    @Column(name = "last_modified_date")
    private String lastModifiedDate;

    @Column(name = "rule_master_id")
    private String ruleMasterId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getWorkflowCode() {
        return workflowCode;
    }

    public void setWorkflowCode(String workflowCode) {
        this.workflowCode = workflowCode;
    }

    public String getWorkflowCodeNm() {
        return workflowCodeNm;
    }

    public void setWorkflowCodeNm(String workflowCodeNm) {
        this.workflowCodeNm = workflowCodeNm;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getSubsidiary() {
        return subsidiary;
    }

    public void setSubsidiary(String subsidiary) {
        this.subsidiary = subsidiary;
    }

    public String getDivision() {
        return division;
    }

    public void setDivision(String division) {
        this.division = division;
    }

    public String getDivisionNm() {
        return divisionNm;
    }

    public void setDivisionNm(String divisionNm) {
        this.divisionNm = divisionNm;
    }

    public String getRuleLevel() {
        return ruleLevel;
    }

    public void setRuleLevel(String ruleLevel) {
        this.ruleLevel = ruleLevel;
    }

    public String getApproveFormat() {
        return approveFormat;
    }

    public void setApproveFormat(String approveFormat) {
        this.approveFormat = approveFormat;
    }

    public String getApproveFormatNm() {
        return approveFormatNm;
    }

    public void setApproveFormatNm(String approveFormatNm) {
        this.approveFormatNm = approveFormatNm;
    }

    public String getApproveCondition() {
        return approveCondition;
    }

    public void setApproveCondition(String approveCondition) {
        this.approveCondition = approveCondition;
    }

    public String getApproveConditionNm() {
        return approveConditionNm;
    }

    public void setApproveConditionNm(String approveConditionNm) {
        this.approveConditionNm = approveConditionNm;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getRoleNm() {
        return roleNm;
    }

    public void setRoleNm(String roleNm) {
        this.roleNm = roleNm;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(String updatedDate) {
        this.updatedDate = updatedDate;
    }

    public String getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(String lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public String getRuleMasterId() {
        return ruleMasterId;
    }

    public void setRuleMasterId(String ruleMasterId) {
        this.ruleMasterId = ruleMasterId;
    }

    @Override
    public String toString() {
        return "ApprovalRule [id=" + id + ", channel=" + channel + ", workflowCode=" + workflowCode
                + ", workflowCodeNm=" + workflowCodeNm + ", country=" + country + ", subsidiary=" + subsidiary
                + ", division=" + division + ", divisionNm=" + divisionNm + ", ruleLevel=" + ruleLevel
                + ", approveFormat=" + approveFormat + ", approveFormatNm=" + approveFormatNm + ", approveCondition="
                + approveCondition + ", approveConditionNm=" + approveConditionNm + ", status=" + status + ", role="
                + role + ", roleNm=" + roleNm + ", createdAt=" + createdAt + ", updatedAt=" + updatedAt
                + ", createdDate=" + createdDate + ", updatedDate=" + updatedDate + ", lastModifiedDate="
                + lastModifiedDate + ", ruleMasterId=" + ruleMasterId + "]";
    }    
}