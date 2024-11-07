package com.samsung.ciam.models;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 1. 파일명   : UserAgreedConsents.java
 * 2. 패키지   : com.samsung.ciam.models
 * 3. 설명     : 사용자 약관 동의 이력 테이블 (JPA)
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
@Table(name = "user_agreed_consents")
public class UserAgreedConsents implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  //고유 식별자

    @Column(name = "consent_id", nullable = false)
    private Long consentId; //약관 마스터 ID

    @Column(name = "consent_content_id", nullable = false)
    private Long consentContentId; //약관 컨텐츠 마스터 ID

    @Column(name = "uid", nullable = false)
    private String uid; //동의한 사용자 UID

    @Column(name = "status", nullable = false)
    private String status; //동의상태(동의,거부)

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt; //등록일시

    @Column(name = "updated_at")
    private LocalDateTime updatedAt; //수정일시

    @Transient
    private Consent consent; //약관 마스터 객체 , 실제 물리 컬럼 존재 X

    @Transient
    private ConsentContent content; //약관 컨텐츠 객체, 실제 물리 컬럼 존재 X

    @Transient
    private Double version; //약관 버전, 실제 물리 컬럼 존재 X

    @Transient
    private String agreementDate; //약관 동의일시, 실제 물리 컬럼 존재 X

    @Transient
    private String location;  //국가, 실제 물리 컬럼 존재 X

    @Transient
    private UserAgreedConsents agreement; //사용자 동의 객체, 실제 물리 컬럼 존재 X

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