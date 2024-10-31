package com.samsung.ciam.models;

import jakarta.persistence.*;

@Entity
@Table(name = "channel_approvers")
public class ChannelApprovers {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "channel")
    private String channel;

    @Column(name = "auto_approve")
    private boolean autoApprove;

    @Column(name = "approval_line")
    private int approvalLine;

    @Column(name = "rule")
    private int rule;

    @Column(name = "country")
    private String country;

    @Column(name = "approver_email")
    private String approverEmail;

    @Column(name = "uid")
    private String uid;

    // Getter and Setter methods

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

    public boolean isAutoApprove() {
        return autoApprove;
    }

    public void setAutoApprove(boolean autoApprove) {
        this.autoApprove = autoApprove;
    }

    public int getApprovalLine() {
        return approvalLine;
    }

    public void setApprovalLine(int approvalLine) {
        this.approvalLine = approvalLine;
    }

    public int getRule() {
        return rule;
    }

    public void setRule(int rule) {
        this.rule = rule;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getApproverEmail() {
        return approverEmail;
    }

    public void setApproverEmail(String approverEmail) {
        this.approverEmail = approverEmail;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}