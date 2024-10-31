package com.samsung.ciam.models;

import com.fasterxml.jackson.databind.JsonNode;
import com.samsung.ciam.common.core.component.JsonNodeConverter;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneId;

import lombok.NoArgsConstructor;

@NoArgsConstructor
@Entity
@Table(name = "channel_approval_statuses")
public class ChannelApprovalStatuses {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "login_id", nullable = false)
    private String loginId;

    @Column(name = "login_uid", nullable = false)
    private String loginUid;

    @Column(name = "channel", nullable = false)
    private String channel;

    @Column(name = "channel_approver_id")
    private long channelApproverId;

    @Column(name = "approval_line")
    private Integer approvalLine;

    @Column(name = "match_type", nullable = false)
    private String matchType;

    @Column(name = "request_type", nullable = false)
    private String requestType;

    @Column(name = "status", nullable = false)
    private String status = "pending";

    @Column(name = "reason")
    private String reason;

    @Column(name = "approval_date")
    private LocalDateTime approvalDate;

    @Column(name = "approver_name", nullable = false)
    private String approverName;

    @Column(name = "approver_email", nullable = false)
    private String approverEmail = "default@example.com";

    @Column(name = "approver_uid", nullable = false)
    private String approverUid;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "require_update_token")
    private String requireUpdateToken;

    @Column(name = "require_update_expiry")
    private LocalDateTime requireUpdateExpiry;

    @Column(name = "require_update_status")
    private String requireUpdateStatus;

    @Column(name = "processing_uid")
    private String processingUid;

    @Column(name = "processing_login_id")
    private String processingLoginId;

    /*@Convert(converter = JsonNodeConverter.class)
    @Column(name = "attachment_files")
    private JsonNode attachmentFiles;

    @Convert(converter = JsonNodeConverter.class)
    @Column(name = "products")
    private JsonNode products;*/

    @Column(name = "subsidiary")
    private String subsidiary;

    // Contructor
    public ChannelApprovalStatuses(
        String loginId,
        String loginUid,
        String channel,
        Long channelApproverId,
        Integer approvalLine,
        String matchType,
        String requestType,
        String status,
        String approverName,
        String approverEmail,
        String approverUid,
        String subsidiary
    ){
        this.loginId = loginId;
        this.loginUid = loginUid;
        this.channel = channel;
        this.channelApproverId = channelApproverId;
        this.approvalLine = approvalLine;
        this.matchType = matchType;
        this.requestType = requestType;
        this.status = status;
        this.approverName = approverName;
        this.approverEmail = approverEmail;
        this.approverUid = approverUid;
        this.subsidiary = subsidiary;
    }

    // Getters and Setters
    // (자동 생성되는 getter, setter 코드)


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

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }


    public long getChannelApproverId() {
        return channelApproverId;
    }

    public void setChannelApproverId(long channelApproverId) {
        this.channelApproverId = channelApproverId;
    }

    public Integer getApprovalLine() {
        return approvalLine;
    }

    public void setApprovalLine(Integer approvalLine) {
        this.approvalLine = approvalLine;
    }

    public String getMatchType() {
        return matchType;
    }

    public void setMatchType(String matchType) {
        this.matchType = matchType;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
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

    public String getRequireUpdateToken() {
        return requireUpdateToken;
    }

    public void setRequireUpdateToken(String requireUpdateToken) {
        this.requireUpdateToken = requireUpdateToken;
    }

    public LocalDateTime getRequireUpdateExpiry() {
        return requireUpdateExpiry;
    }

    public void setRequireUpdateExpiry(LocalDateTime requireUpdateExpiry) {
        this.requireUpdateExpiry = requireUpdateExpiry;
    }

    public String getRequireUpdateStatus() {
        return requireUpdateStatus;
    }

    public void setRequireUpdateStatus(String requireUpdateStatus) {
        this.requireUpdateStatus = requireUpdateStatus;
    }

    public String getProcessingUid() {
        return processingUid;
    }

    public void setProcessingUid(String processingUid) {
        this.processingUid = processingUid;
    }

    public String getProcessingLoginId() {
        return processingLoginId;
    }

    public void setProcessingLoginId(String processingLoginId) {
        this.processingLoginId = processingLoginId;
    }

    @PrePersist
    protected void onCreate() {
        ZoneId zoneId = ZoneId.of("Asia/Seoul");
        this.createdAt = LocalDateTime.now(zoneId);
        this.updatedAt = LocalDateTime.now(zoneId);
    }

    @PreUpdate
    protected void onUpdate() {
        ZoneId zoneId = ZoneId.of("Asia/Seoul");
        this.updatedAt = LocalDateTime.now(zoneId);
    }

    /*public JsonNode getAttachmentFiles() {
        return attachmentFiles;
    }

    public void setAttachmentFiles(JsonNode attachmentFiles) {
        this.attachmentFiles = attachmentFiles;
    }

    public JsonNode getProducts() {
        return products;
    }

    public void setProducts(JsonNode products) {
        this.products = products;
    }*/

    public String getSubsidiary() {
        return subsidiary;
    }

    public void setSubsidiary(String subsidiary) {
        this.subsidiary = subsidiary;
    }


}