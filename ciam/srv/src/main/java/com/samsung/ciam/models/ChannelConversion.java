package com.samsung.ciam.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "channel_conversions")
public class ChannelConversion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "channel")
    private String channel;

    @Column(name = "convertLoginId")
    private String convertLoginId;

    @Column(name = "convertUid")
    private String convertUid;

    @Column(name = "is_ciam_user")
    private Boolean isCiamUser;

    @Column(name = "status")
    private String status;

    @Column(name = "completion_date")
    private LocalDateTime completionDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Getters and Setters
    // ...


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

    public String getConvertLoginId() {
        return convertLoginId;
    }

    public void setConvertLoginId(String convertLoginId) {
        this.convertLoginId = convertLoginId;
    }

    public String getConvertUid() {
        return convertUid;
    }

    public void setConvertUid(String convertUid) {
        this.convertUid = convertUid;
    }

    public Boolean getCiamUser() {
        return isCiamUser;
    }

    public void setCiamUser(Boolean ciamUser) {
        isCiamUser = ciamUser;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(LocalDateTime completionDate) {
        this.completionDate = completionDate;
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
}