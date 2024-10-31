package com.samsung.ciam.models;


import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(name = "new_subsidiary")
public class NewSubsidiary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "country_code")
    private String countryCode;

    @Column(name = "request_channel")
    private String requestChannel;

    @Column(name = "erp_useyn")
    private String erpUseYn;

    @Column(name = "changer_id")
    private String changerId;

    @Column(name = "change_date")
    private LocalDateTime changeDate;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "company_abbreviation")
    private String companyAbbreviation;

    @Column(name = "creator_id")
    private String creatorId;

    @Column(name = "cmdm_useyn")
    private String cmdmUseYn;

    @Column(name = "company_code")
    private String companyCode;

    @Column(name = "create_date")
    private LocalDateTime createDate;

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