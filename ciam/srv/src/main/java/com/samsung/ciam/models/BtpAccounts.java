package com.samsung.ciam.models;

import java.sql.*;
import java.io.Serializable;

import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@ToString
@Getter
@Entity
@Table(name = "btp_accounts")
public class BtpAccounts implements Serializable {
    @Id
    @Column(nullable = false)
    private Long id;  // bigserial

    @Column
    private String bpid;

    @Column
    private String vendorcode;

    @Column
    private String bizregno1;

    @Column
    private String bizregno02;

    @Column
    private String vatno;

    @Column
    private String ciscode;

    @Column
    private String name;

    @Column(name = "name_search")
    private String nameSearch;

    @Column(name = "name_gl")
    private String nameGl;

    @Column
    private String phonenumber1;

    @Column
    private String phonenumber2;

    @Column
    private String faxno;

    @Column
    private String accountwebsite;

    @Column
    private String email;

    @Column
    private String country;

    @Column(name = "street_address")
    private String streetAddress;

    @Column
    private String city;

    @Column
    private String state;

    @Column(name = "street_gl")
    private String streetGl;

    @Column(name = "district_gl")
    private String districtGl;

    @Column(name = "zip_code")
    private String zipCode;

    @Column
    private String representative;

    @Column(name = "\"validStatus\"")
    private String validStatus;

    @Column
    private String source;

    @Column
    private String type;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @Column
    private String dunsno;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBpid() {
        return bpid;
    }

    public void setBpid(String bpid) {
        this.bpid = bpid;
    }

    public String getVendorcode() {
        return vendorcode;
    }

    public void setVendorcode(String vendorcode) {
        this.vendorcode = vendorcode;
    }

    public String getBizregno1() {
        return bizregno1;
    }

    public void setBizregno1(String bizregno1) {
        this.bizregno1 = bizregno1;
    }

    public String getBizregno02() {
        return bizregno02;
    }

    public void setBizregno02(String bizregno02) {
        this.bizregno02 = bizregno02;
    }

    public String getVatno() {
        return vatno;
    }

    public void setVatno(String vatno) {
        this.vatno = vatno;
    }

    public String getCiscode() {
        return ciscode;
    }

    public void setCiscode(String ciscode) {
        this.ciscode = ciscode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNameSearch() {
        return nameSearch;
    }

    public void setNameSearch(String nameSearch) {
        this.nameSearch = nameSearch;
    }

    public String getNameGl() {
        return nameGl;
    }

    public void setNameGl(String nameGl) {
        this.nameGl = nameGl;
    }

    public String getPhonenumber1() {
        return phonenumber1;
    }

    public void setPhonenumber1(String phonenumber1) {
        this.phonenumber1 = phonenumber1;
    }

    public String getPhonenumber2() {
        return phonenumber2;
    }

    public void setPhonenumber2(String phonenumber2) {
        this.phonenumber2 = phonenumber2;
    }

    public String getFaxno() {
        return faxno;
    }

    public void setFaxno(String faxno) {
        this.faxno = faxno;
    }

    public String getAccountwebsite() {
        return accountwebsite;
    }

    public void setAccountwebsite(String accountwebsite) {
        this.accountwebsite = accountwebsite;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getStreetGl() {
        return streetGl;
    }

    public void setStreetGl(String streetGl) {
        this.streetGl = streetGl;
    }

    public String getDistrictGl() {
        return districtGl;
    }

    public void setDistrictGl(String districtGl) {
        this.districtGl = districtGl;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getRepresentative() {
        return representative;
    }

    public void setRepresentative(String representative) {
        this.representative = representative;
    }

    public String getValidStatus() {
        return validStatus;
    }

    public void setValidStatus(String validStatus) {
        this.validStatus = validStatus;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getDunsno() {
        return dunsno;
    }

    public void setDunsno(String dunsno) {
        this.dunsno = dunsno;
    }
}
