package com.samsung.ciam.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 1. 파일명   : VerificationEmploy.java
 * 2. 패키지   : com.samsung.ciam.models
 * 3. 설명     : 재직인증 유저 관리 테이블 (JPA)
 * 4. 작성자   : 서정환
 * 5. 작성일자 : 2024. 11. 04.
 * 6. 히스토리 :
 * <p>
 * -----------------------------------------------------------------
 * <p>
 * 날짜         | 이름         | 설명
 * <p>
 * -------------|--------------|------------------------------------
 * <p>
 * 2024. 11. 04 | 서정환       | 최초작성
 * <p>
 * -----------------------------------------------------------------
 */
@Entity
@Table(name = "verification_employ")
public class VerificationEmploy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; //고유 식별자

    @Column(name = "channel", nullable = false)
    private String channel; //채널

    @Column(name = "bpid")
    private String bpid; //회사코드

    @Column(name = "bp_name")
    private String bpName; //회사명

    @Column(name = "requestor_uid")
    private String requestorUid; //요청자 ID

    @Column(name = "requestor_email")
    private String requestorEmail; //요청자 이메일

    @Column(name = "requestor_role")
    private String requestorRole; //요청자 ROLE

    @Column(name = "request_date")
    private LocalDateTime requestDate; //요청날짜

    @Column(name = "request_expired_date")
    private LocalDateTime requestExpiredDate; //요청만료일시

    @Column(name = "approval_date")
    private LocalDateTime approvalDate; //승인 날짜

    @Column(name = "approver_name")
    private String approverName; //승인자명

    @Column(name = "approver_email")
    private String approverEmail; //승인자 이메일

    @Column(name = "approver_uid")
    private String approverUid; //승인자UID

    @Column(name = "approver_role")
    private String approverRole; //승인자 Role
    
    @Column(name = "reject_reason")
    private String rejectReason; //반려사유

    @Column(name = "user_status")
    private String userStatus; //유저상태

    @Column(name = "view_status")
    private String viewStatus; //VIEW 상태 -> open/close

    @Column(name = "if_yyyymm")
    private String if_yyyymm; //날짜

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

    public LocalDateTime getRequestExpiredDate() {
        return requestExpiredDate;
    }

    public void setRequestExpiredDate(LocalDateTime requestExpiredDate) {
        this.requestExpiredDate = requestExpiredDate;
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

    public String getApproverRole() {
        return approverRole;
    }

    public void setApproverRole(String approverRole) {
        this.approverRole = approverRole;
    }

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }
    
    public String getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(String userStatus) {
        this.userStatus = userStatus;
 
    }

    public String getViewStatus() {
        return viewStatus;
    }

    public void setViewStatus(String viewStatus) {
        this.viewStatus = viewStatus;
    }


    public String getIf_yyyymm() {
        return if_yyyymm;
    }

    public void setIf_yyyymm(String if_yyyymm) {
        this.if_yyyymm = if_yyyymm;
    }

}