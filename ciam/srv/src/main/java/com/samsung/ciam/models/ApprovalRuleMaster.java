package com.samsung.ciam.models;

import jakarta.persistence.*;

@Entity
@Table(name = "approval_rule_master")
public class ApprovalRuleMaster {

    @Id
    @Column(name = "rule_master_id", length = 50, nullable = false)
    private String ruleMasterId;

    @Column(name = "workflow_code", length = 20)
    private String workflowCode;

    @Column(name = "channel", length = 50, nullable = false)
    private String channel;

    @Column(name = "country", length = 50)
    private String country;

    @Column(name = "subsidiary", length = 50)
    private String subsidiary;

    @Column(name = "division", length = 50)
    private String division;

    @Column(name = "created_at")
    private String createdAt;

    @Column(name = "updated_at")
    private String updatedAt;

    @Column(name = "stage")
    private int stage;

    public String getRuleMasterId() {
        return ruleMasterId;
    }

    public void setRuleMasterId(String ruleMasterId) {
        this.ruleMasterId = ruleMasterId;
    }

    public String getWorkflowCode() {
        return workflowCode;
    }

    public void setWorkflowCode(String workflowCode) {
        this.workflowCode = workflowCode;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
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

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int getStage() {
        return stage;
    }

    public void setStage(int stage) {
        this.stage = stage;
    }

    @Override
    public String toString() {
        return "ApprovalRuleMaster [ruleMasterId=" + ruleMasterId + ", workflowCode=" + workflowCode + ", channel="
                + channel + ", country=" + country + ", subsidiary=" + subsidiary + ", division=" + division
                + ", createdAt=" + createdAt + ", updatedAt=" + updatedAt + ", stage=" + stage + "]";
    }
}