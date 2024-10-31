package com.samsung.ciam.models;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(name = "user_agreed_consents")
public class UserAgreedConsents implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "consent_id", nullable = false)
    private Long consentId;

    @Column(name = "consent_content_id", nullable = false)
    private Long consentContentId;

    @Column(name = "uid", nullable = false)
    private String uid;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Transient
    private Consent consent;

    @Transient
    private ConsentContent content;

    @Transient
    private Double version;

    @Transient
    private String agreementDate;

    @Transient
    private String location;

    @Transient
    private UserAgreedConsents agreement;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getConsentId() {
        return consentId;
    }

    public void setConsentId(Long consentId) {
        this.consentId = consentId;
    }

    public Long getConsentContentId() {
        return consentContentId;
    }

    public void setConsentContentId(Long consentContentId) {
        this.consentContentId = consentContentId;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public Consent getConsent() {
        return consent;
    }

    public void setConsent(Consent consent) {
        this.consent = consent;
    }

    public ConsentContent getContent() {
        return content;
    }

    public void setContent(ConsentContent content) {
        this.content = content;
    }

    // 기본 생성자를 직접 추가
    public UserAgreedConsents() {
    }

    public Double getVersion() {
        return version;
    }

    public void setVersion(Double version) {
        this.version = version;
    }

    public String getAgreementDate() {
        return agreementDate;
    }

    public void setAgreementDate(String agreementDate) {
        this.agreementDate = agreementDate;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public UserAgreedConsents getAgreement() {
        return agreement;
    }

    public void setAgreement(UserAgreedConsents agreement) {
        this.agreement = agreement;
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

}