package com.samsung.ciam.models;

import jakarta.persistence.*;

import java.util.*;
import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.ObjectMapper;

@Entity
@Table(name = "channels")
public class Channels {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "channel_name", nullable = false)
    private String channelName;

    @Column(name = "auto_approval", nullable = false)
    private Boolean autoApproval;

    @Column(name = "approval_method", nullable = false)
    private String approvalMethod;

    @Column(name = "next_channel")
    private String nextChannel;

    @Column(name = "grouping_updated_at")
    private LocalDateTime groupingUpdatedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "channel_display_name")
    private String channelDisplayName;

    @Column(name = "external_id")
    private String externalId;

    @Column(name = "sfdc_regch")
    private String sfdcRegch;

    @Column(name = "cmdm_regch")
    private String cmdmRegch;

    @Column(name = "config", columnDefinition = "json")
    private String config; // JSON을 String으로 매핑합니다. 필요에 따라 Map으로 변환할 수 있습니다.

    @Column(name = "channel_type")
    private String channelType;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public Boolean getAutoApproval() {
        return autoApproval;
    }

    public void setAutoApproval(Boolean autoApproval) {
        this.autoApproval = autoApproval;
    }

    public String getApprovalMethod() {
        return approvalMethod;
    }

    public void setApprovalMethod(String approvalMethod) {
        this.approvalMethod = approvalMethod;
    }

    public String getNextChannel() {
        return nextChannel;
    }

    public void setNextChannel(String nextChannel) {
        this.nextChannel = nextChannel;
    }

    public LocalDateTime getGroupingUpdatedAt() {
        return groupingUpdatedAt;
    }

    public void setGroupingUpdatedAt(LocalDateTime groupingUpdatedAt) {
        this.groupingUpdatedAt = groupingUpdatedAt;
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

    public String getChannelDisplayName() {
        return channelDisplayName;
    }

    public void setChannelDisplayName(String channelDisplayName) {
        this.channelDisplayName = channelDisplayName;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public String getSfdcRegch() {
        return sfdcRegch;
    }

    public void setSfdcRegch(String sfdcRegch) {
        this.sfdcRegch = sfdcRegch;
    }

    public String getCmdmRegch() {
        return cmdmRegch;
    }

    public void setCmdmRegch(String cmdmRegch) {
        this.cmdmRegch = cmdmRegch;
    }

    public String getConfig() {
        return config;
    }

    @SuppressWarnings("unchecked")
    public Map<String,Object> getConfigMap() {
        ObjectMapper mapper = new ObjectMapper();
        try{
            Map<String,Object> mapVal = mapper.readValue(this.getConfig(), Map.class);
            return mapVal;
        } catch (java.io.IOException e) {
            return new HashMap<>();
        }
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public String getChannelType() {
        return channelType;
    }

    public void setChannelType(String channelType) {
        this.channelType = channelType;
    }
}
