package com.samsung.ciam.models;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.sql.Timestamp;

@Entity
@Table(name = "channel_invitations")
public class ChannelInvitation implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // bigserial

    @Column(name = "login_id")
    private String loginId;

    @Column(name = "login_uid")
    private String loginUid;

    @Column(name = "requestor_id")
    private String requestorId;

    @Column(name = "requestor_uid")
    private String requestorUid;

    @Column
    private String bpid;

    @Column(name = "company_source")
    private String companySource;

    @Column
    private String channel;

    @Column(name = "token")
    private String token;

    @Column(name = "expiry")
    private Timestamp expiry;

    @Column(name = "status")
    private String status;

    @Column(name = "status_updated")
    private Timestamp statusUpdated;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @Column(name = "rejected_reason")
    private String rejectedReason;

    @Column(name = "rejected_id")
    private String rejectedId;

    @Column(name = "approved_date")
    private Timestamp approvedDate;

    @Column(name = "approved_id")
    private String approvedId;

    @Column(name = "rejected_date")
    private Timestamp rejectedDate;

    @Column(name = "subsidiary")
    private String subsidiary;

    @Column(name = "company_name")
    private String companyName;

    // Getter 및 Setter 메서드는 Lombok에 의해 자동으로 생성됩니다.


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLoginId() {
        return loginId;
    }

    public void setLoginId(String loginId) {
        this.loginId = loginId;
    }

    public String getLoginUid() {
        return loginUid;
    }

    public void setLoginUid(String loginUid) {
        this.loginUid = loginUid;
    }

    public String getRequestorId() {
        return requestorId;
    }

    public void setRequestorId(String requestorId) {
        this.requestorId = requestorId;
    }

    public String getRequestorUid() {
        return requestorUid;
    }

    public void setRequestorUid(String requestorUid) {
        this.requestorUid = requestorUid;
    }

    public String getBpid() {
        return bpid;
    }

    public void setBpid(String bpid) {
        this.bpid = bpid;
    }

    public String getCompanySource() {
        return companySource;
    }

    public void setCompanySource(String companySource) {
        this.companySource = companySource;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Timestamp getExpiry() {
        return expiry;
    }

    public void setExpiry(Timestamp expiry) {
        this.expiry = expiry;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getStatusUpdated() {
        return statusUpdated;
    }

    public void setStatusUpdated(Timestamp statusUpdated) {
        this.statusUpdated = statusUpdated;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getRejectedReason() {
        return rejectedReason;
    }

    public void setRejectedReason(String rejectedReason) {
        this.rejectedReason = rejectedReason;
    }

    public String getRejectedId() {
        return rejectedId;
    }

    public void setRejectedId(String rejectedId) {
        this.rejectedId = rejectedId;
    }

    public Timestamp getApprovedDate() {
        return approvedDate;
    }

    public void setApprovedDate(Timestamp approvedDate) {
        this.approvedDate = approvedDate;
    }

    public String getApprovedId() {
        return approvedId;
    }

    public void setApprovedId(String approvedId) {
        this.approvedId = approvedId;
    }

    public Timestamp getRejectedDate() {
        return rejectedDate;
    }

    public void setRejectedDate(Timestamp rejectedDate) {
        this.rejectedDate = rejectedDate;
    }

    public String getSubsidiary() {
        return subsidiary;
    }

    public void setSubsidiary(String subsidiary) {
        this.subsidiary = subsidiary;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
}

