package com.samsung.ciam.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 1. 파일명   : WfMaster.java
 * 2. 패키지   : com.samsung.ciam.models
 * 3. 설명     : 승인 요청 테이블 (JPA)
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
@Table(name = "wf_master")
public class WfMaster {

    @Id
    @Column(name = "wf_id", length = 20, nullable = false)
    private String wfId; //Approval List에 인입되는 모든 승인 요청건에 대한 Unique ID

    @Column(name = "channel", length = 50, nullable = false)
    private String channel; //승인 요청 건이 종속되는 채널

    @Column(name = "workflow_code", length = 20)
    private String workflowCode; //request Type (7개 항목, 추가 가능)

    @Column(name = "requestor_id", length = 255)
    private String requestorId; //승인을 요청한 User ID

    @Column(name = "requestor_email", length = 255)
    private String requestorEmail; //승인을 요청한 User Email

    @Column(name = "requestor_role", length = 20)
    private String requestorRole; //승인요청 유저 권한

    @Column(name = "requestor_company_name", length = 255)
    private String requestorCompanyName; //승인을 요청한 유저 소속 회사명

    @Column(name = "requestor_company_code", length = 255)
    private String requestorCompanyCode; //승인을 요청한 유저 소속 회사 코드

    @Column(name = "approver_id", length = 255)
    private String approverId; //최종 승인/반려 처리자 ID

    @Column(name = "approver_email", length = 255)
    private String approverEmail; //최종 승인/반려 처리자 Email

    @Column(name = "status", length = 20)
    private String status; //해당 승인 요청 건의 상태 (pending/approved/rejected)

    @Column(name = "wf_max_level")
    private Integer wfLevel; //승인 레벨 1, 2, 3, 4, 5

    @Column(name = "approved_level")
    private Integer approveLevel; //1, 2 (승인 완료된 레벨)

    @Column(name = "requested_date")
    private LocalDateTime requestedDate; //요청 발송 일자

    @Column(name = "rejected_date")
    private LocalDateTime rejectedDate; //반려 일자

    @Column(name = "approved_date")
    private LocalDateTime approvedDate; //승인 일자

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
}