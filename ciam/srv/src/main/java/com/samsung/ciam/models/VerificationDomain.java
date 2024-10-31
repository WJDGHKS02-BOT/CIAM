package com.samsung.ciam.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "verification_domains")
public class VerificationDomain {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "channel")
    private String channel;

    @Column(name = "email_domain")
    private String emailDomain;

    @Column(name = "status")
    private String status;

    @Column(name = "bpid")
    private String bpid;

    @Column(name = "bp_name")
    private String bpName;

    @Column(name = "requestor_uid")
    private String requestorUid;

    @Column(name = "requestor_email")
    private String requestorEmail;

    @Column(name = "requestor_role")
    private String requestorRole;

    @Column(name = "request_date")
    private LocalDateTime requestDate;

    @Column(name = "approval_date")
    private LocalDateTime approvalDate;

    @Column(name = "approver_name")
    private String approverName;

    @Column(name = "approver_email")
    private String approverEmail;

    @Column(name = "approver_uid")
    private String approverUid;

    @Column(name = "reject_reason")
    private String rejectReason;

    // Getters and Setters

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

    public String getEmailDomain() {
        return emailDomain;
    }

    public void setEmailDomain(String emailDomain) {
        this.emailDomain = emailDomain;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBpid() {
        return bpid;
    }

    public void setBpid(String bpid) {
        this.bpid = bpid;
    }

    public String getBpName() {
        return bpName;
    }

    public void setBpName(String bpName) {
        this.bpName = bpName;
    }

    public String getRequestorUid() {
        return requestorUid;
    }

    public void setRequestorUid(String requestorUid) {
        this.requestorUid = requestorUid;
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

    public LocalDateTime getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(LocalDateTime requestDate) {
        this.requestDate = requestDate;
    }

    public LocalDateTime getApprovalDate() {
        return approvalDate;
    }

    public void setApprovalDate(LocalDateTime approvalDate) {
        this.approvalDate = approvalDate;
    }

    public String getApproverName() {
        return approverName;
    }

    public void setApproverName(String approverName) {
        this.approverName = approverName;
    }

    public String getApproverEmail() {
        return approverEmail;
    }

    public void setApproverEmail(String approverEmail) {
        this.approverEmail = approverEmail;
    }

    public String getApproverUid() {
        return approverUid;
    }

    public void setApproverUid(String approverUid) {
        this.approverUid = approverUid;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }
}