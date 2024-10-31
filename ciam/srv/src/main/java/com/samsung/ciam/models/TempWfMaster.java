package com.samsung.ciam.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "temp_wf_master")
public class TempWfMaster {

    @Id
    @Column(name = "wf_id", length = 20, nullable = false)
    private String wfId;

    @Column(name = "channel", length = 50, nullable = false)
    private String channel;

    @Column(name = "workflow_code", length = 20)
    private String workflowCode;

    @Column(name = "requestor_id", length = 255)
    private String requestorId;

    @Column(name = "requestor_email", length = 255)
    private String requestorEmail;

    @Column(name = "requestor_role", length = 20)
    private String requestorRole;

    @Column(name = "requestor_company_name", length = 255)
    private String requestorCompanyName;

    @Column(name = "requestor_company_code", length = 255)
    private String requestorCompanyCode;

    @Column(name = "approver_id", length = 255)
    private String approverId;

    @Column(name = "approver_email", length = 255)
    private String approverEmail;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "wf_max_level")
    private Integer wfLevel;

    @Column(name = "approved_level")
    private Integer approveLevel;

    @Column(name = "requested_date")
    private LocalDateTime requestedDate;

    @Column(name = "rejected_date")
    private LocalDateTime rejectedDate;

    @Column(name = "approved_date")
    private LocalDateTime approvedDate;

    @Column(name = "rule_master_id")
    private String ruleMasterId;

    // Getters and setters

    public String getWfId() {
        return wfId;
    }

    public void setWfId(String wfId) {
        this.wfId = wfId;
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

    public String getRequestorId() {
        return requestorId;
    }

    public void setRequestorId(String requestorId) {
        this.requestorId = requestorId;
    }

    public String getRequestorEmail() {
        return requestorEmail;
    }

    public void setRequestorEmail(String requestorEmail) {
        this.requestorEmail = requestorEmail;
    }

    public String getRequestorRole() {
        return requestorRole;
    }

    public void setRequestorRole(String requestorRole) {
        this.requestorRole = requestorRole;
    }

    public String getRequestorCompanyName() {
        return requestorCompanyName;
    }

    public void setRequestorCompanyName(String requestorCompanyName) {
        this.requestorCompanyName = requestorCompanyName;
    }

    public String getRequestorCompanyCode() {
        return requestorCompanyCode;
    }

    public void setRequestorCompanyCode(String requestorCompanyCode) {
        this.requestorCompanyCode = requestorCompanyCode;
    }

    public String getApproverId() {
        return approverId;
    }

    public void setApproverId(String approverId) {
        this.approverId = approverId;
    }

    public String getApproverEmail() {
        return approverEmail;
    }

    public void setApproverEmail(String approverEmail) {
        this.approverEmail = approverEmail;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getWfLevel() {
        return wfLevel;
    }

    public void setWfLevel(Integer wfLevel) {
        this.wfLevel = wfLevel;
    }

    public Integer getApproveLevel() {
        return approveLevel;
    }

    public void setApproveLevel(Integer approveLevel) {
        this.approveLevel = approveLevel;
    }

    public LocalDateTime getRequestedDate() {
        return requestedDate;
    }

    public void setRequestedDate(LocalDateTime requestedDate) {
        this.requestedDate = requestedDate;
    }

    public LocalDateTime getRejectedDate() {
        return rejectedDate;
    }

    public void setRejectedDate(LocalDateTime rejectedDate) {
        this.rejectedDate = rejectedDate;
    }

    public LocalDateTime getApprovedDate() {
        return approvedDate;
    }

    public void setApprovedDate(LocalDateTime approvedDate) {
        this.approvedDate = approvedDate;
    }

    public String getRuleMasterId() {
        return ruleMasterId;
    }

    public void setRuleMasterId(String ruleMasterId) {
        this.ruleMasterId = ruleMasterId;
    }
}