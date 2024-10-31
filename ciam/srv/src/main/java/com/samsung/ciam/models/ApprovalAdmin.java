package com.samsung.ciam.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "approval_admins")
public class ApprovalAdmin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "uid", nullable = false, length = 255)
    private String uid;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "type", length = 20)
    private String type;

    @Column(name = "type_nm", length = 20)
    private String typeNm;

    @Column(name = "channel", length = 50)
    private String channel;

    @Column(name = "country", length = 50)
    private String country;

    @Column(name = "subsidiary", length = 50)
    private String subsidiary;

    @Column(name = "division", length = 50)
    private String division;

    @Column(name = "company_code", length = 50)
    private String companyCode;

    @Column(name = "active")
    private Boolean active;

    @Column(name = "approval_user", length = 255)
    private String approvalUser;

    @Column(name = "approval_date")
    private LocalDateTime approvalDate;

    @Column(name = "tempapprover")
    private Boolean Tempapprover;

    @Column(name = "tempapprovalrule")
    private String tempapprovalrule;

    @Column(name = "tempapprovalrulelevel")
    private String tempapprovalrulelevel;

    @Column(name = "disabled")
    private Boolean disabled;

    @Column(name = "disabledrule")
    private String disabledrule;

    @Column(name = "disabledrulelevel")
    private String disabledrulelevel;

    @Column(name = "updateuser", length = 255)
    private String updateUser;

    @Column(name = "created_at")
    private String createdAt;

    @Column(name = "updated_at")
    private String updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTypeNm() {
        return typeNm;
    }

    public void setTypeNm(String typeNm) {
        this.typeNm = typeNm;
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

    public String getCompanyCode() {
        return companyCode;
    }

    public void setCompanyCode(String companyCode) {
        this.companyCode = companyCode;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getApprovalUser() {
        return approvalUser;
    }

    public void setApprovalUser(String approvalUser) {
        this.approvalUser = approvalUser;
    }

    public LocalDateTime getApprovalDate() {
        return approvalDate;
    }

    public void setApprovalDate(LocalDateTime approvalDate) {
        this.approvalDate = approvalDate;
    }

    public Boolean getTempapprover() {
        return Tempapprover;
    }

    public void setTempapprover(Boolean tempapprover) {
        Tempapprover = tempapprover;
    }

    public String getTempapprovalrule() {
        return tempapprovalrule;
    }

    public void setTempapprovalrule(String tempapprovalrule) {
        this.tempapprovalrule = tempapprovalrule;
    }

    public String getTempapprovalrulelevel() {
        return tempapprovalrulelevel;
    }

    public void setTempapprovalrulelevel(String tempapprovalrulelevel) {
        this.tempapprovalrulelevel = tempapprovalrulelevel;
    }

    public Boolean getDisabled() {
        return disabled;
    }

    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }

    public String getDisabledrule() {
        return disabledrule;
    }

    public void setDisabledrule(String disabledrule) {
        this.disabledrule = disabledrule;
    }

    public String getDisabledrulelevel() {
        return disabledrulelevel;
    }

    public void setDisabledrulelevel(String disabledrulelevel) {
        this.disabledrulelevel = disabledrulelevel;
    }

    public String getUpdateUser() {
        return updateUser;
    }

    public void setUpdateUser(String updateUser) {
        this.updateUser = updateUser;
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

    @Override
    public String toString() {
        return "ApprovalAdmin [id=" + id + ", uid=" + uid + ", email=" + email + ", type=" + type + ", typeNm=" + typeNm
                + ", channel=" + channel + ", country=" + country + ", subsidiary=" + subsidiary + ", division="
                + division + ", companyCode=" + companyCode + ", active=" + active + ", approvalUser=" + approvalUser
                + ", approvalDate=" + approvalDate + ", Tempapprover=" + Tempapprover + ", tempapprovalrule="
                + tempapprovalrule + ", tempapprovalrulelevel=" + tempapprovalrulelevel + ", disabled=" + disabled
                + ", disabledrule=" + disabledrule + ", disabledrulelevel=" + disabledrulelevel + ", updateUser="
                + updateUser + ", createdAt=" + createdAt + ", updatedAt=" + updatedAt + "]";
    }

}