package com.samsung.ciam.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "wf_list")
public class WfList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "wf_id", length = 20, nullable = false)
    private String wfId;

    @Column(name = "workflow_code", length = 50, nullable = false)
    private String workflowCode;

    @Column(name = "wf_level")
    private Integer level;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "approval_format", length = 255)
    private String approvalFormat;

    @Column(name = "approver_id", length = 255)
    private String approverId;

    @Column(name = "approver_email", length = 255)
    private String approverEmail;

    @Column(name = "approver_role", length = 255)
    private String approverRole;

    @Column(name = "rejected_date")
    private LocalDateTime rejectedDate;

    @Column(name = "approved_date")
    private LocalDateTime approvedDate;

    // Getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getWfId() {
        return wfId;
    }

    public void setWfId(String wfId) {
        this.wfId = wfId;
    }

    public String getWorkflowCode() {
        return workflowCode;
    }

    public void setWorkflowCode(String workflowCode) {
        this.workflowCode = workflowCode;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getApprovalFormat() {
        return approvalFormat;
    }

    public void setApprovalFormat(String approvalFormat) {
        this.approvalFormat = approvalFormat;
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

    public String getApproverRole() {
        return approverRole;
    }

    public void setApproverRole(String approverRole) {
        this.approverRole = approverRole;
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
}