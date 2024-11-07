package com.samsung.ciam.models;


import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 1. 파일명   : NewSubsidiary.java
 * 2. 패키지   : com.samsung.ciam.models
 * 3. 설명     : 법인 회사 코드 관리 테이블 (JPA)
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
@Table(name = "new_subsidiary")
public class NewSubsidiary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; //고유 식별자

    @Column(name = "country_code")
    private String countryCode; //국가코드

    @Column(name = "request_channel")
    private String requestChannel; //등록 채널 시스템

    @Column(name = "erp_useyn")
    private String erpUseYn; //ERP 시스템 사용유무

    @Column(name = "changer_id")
    private String changerId; //이관 시스템 ID

    @Column(name = "change_date")
    private LocalDateTime changeDate; //이관일자

    @Column(name = "company_name")
    private String companyName; //회사명

    @Column(name = "company_abbreviation")
    private String companyAbbreviation; //회사 법인코드

    @Column(name = "creator_id")
    private String creatorId; //등록 시스템 ID

    @Column(name = "cmdm_useyn")
    private String cmdmUseYn; //CMDM 사용유무

    @Column(name = "company_code")
    private String companyCode; //회사코드

    @Column(name = "create_date")
    private LocalDateTime createDate; //생성일자

    @PrePersist
    protected void onCreate() {
        this.createDate = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getRequestChannel() {
        return requestChannel;
    }

    public void setRequestChannel(String requestChannel) {
        this.requestChannel = requestChannel;
    }

    public String getChangerId() {
        return changerId;
    }

    public void setChangerId(String changerId) {
        this.changerId = changerId;
    }

    public LocalDateTime getChangeDate() {
        return changeDate;
    }

    public void setChangeDate(LocalDateTime changeDate) {
        this.changeDate = changeDate;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanyAbbreviation() {
        return companyAbbreviation;
    }

    public void setCompanyAbbreviation(String companyAbbreviation) {
        this.companyAbbreviation = companyAbbreviation;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    public String getCompanyCode() {
        return companyCode;
    }

    public void setCompanyCode(String companyCode) {
        this.companyCode = companyCode;
    }

    public LocalDateTime getCreateDate() {
        return createDate;
    }

    public void setCreateDate(LocalDateTime createDate) {
        this.createDate = createDate;
    }

    public String getErpUseYn() {
        return erpUseYn;
    }

    public void setErpUseYn(String erpUseYn) {
        this.erpUseYn = erpUseYn;
    }

    public String getCmdmUseYn() {
        return cmdmUseYn;
    }

    public void setCmdmUseYn(String cmdmUseYn) {
        this.cmdmUseYn = cmdmUseYn;
    }
}