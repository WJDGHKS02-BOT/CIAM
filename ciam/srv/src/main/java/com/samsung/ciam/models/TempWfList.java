package com.samsung.ciam.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 1. 파일명   : TempWfList.java
 * 2. 패키지   : com.samsung.ciam.models
 * 3. 설명     : 테스트용 승인 요청 상세 테이블 (JPA)
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
@Table(name = "temp_wf_list")
public class TempWfList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id; //리스트 ID

    @Column(name = "wf_id", length = 20, nullable = false)
    private String wfId; //승인요청건 ID (FK)

    @Column(name = "workflow_code", length = 50, nullable = false)
    private String workflowCode; //워크플로우 유형

    @Column(name = "wf_level")
    private Integer level; //승인레벨

    @Column(name = "status", length = 20)
    private String status; //승인상태

    @Column(name = "approval_format", length = 255)
    private String approvalFormat;  //승인포맷

    @Column(name = "approver_id", length = 255)
    private String approverId; //승인/반려 처리할 사람 ID

    @Column(name = "approver_email", length = 255)
    private String approverEmail; //승인/반려 처리할 사람 email

    @Column(name = "approver_role", length = 255)
    private String approverRole; //승인부여 ROLE(권한)

    @Column(name = "rejected_date")
    private LocalDateTime rejectedDate; //반려일시

    @Column(name = "approved_date")
    private LocalDateTime approvedDate; //승인일시

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