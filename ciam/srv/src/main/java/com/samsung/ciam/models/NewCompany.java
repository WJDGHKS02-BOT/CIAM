package com.samsung.ciam.models;

import java.time.LocalDateTime;
import java.time.ZoneId;

import jakarta.persistence.*;

/**
 * 1. 파일명   : NewCompany.java
 * 2. 패키지   : com.samsung.ciam.models
 * 3. 설명     : CMDM 새 회사 등록 관리 테이블 (JPA)
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
@Table(name = "new_companies")
public class NewCompany {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; //고유 식별자

    @Column(name = "bpid")
    private String bpid; //BP ID

    @Column(name = "bizregno1")
    private String bizRegNo1; //사업자 등록번호1

    @Column(name = "bizregno02")
    private String bizRegNo02; //사업자 등록번호2

    @Column(name = "channeltype")
    private String channelType; //채널 유형

    @Column(name = "ciscode")
    private String cisCode; //CIS 코드

    @Column(name = "city")
    private String city; //도시명

    @Column(name = "city_gl")
    private String cityGl; //글로벌 도시명

    @Column(name = "companycode")
    private String companyCode; //회사코드

    @Column(name = "country")
    private String country; //국가

    @Column(name = "\"source\"")
    private String source; //출처

    @Column(name = "description")
    private String description; //설명

    @Column(name = "district_gl")
    private String districtGl; //글로벌 구역

    @Column(name = "dunsno")
    private String dunsNo; //DUNS번호

    @Column(name = "email")
    private String email; //이메일

    @Column(name = "name_gl")
    private String nameGl; //글로벌명

    @Column(name = "faxno")
    private String faxNo; //팩스번호

    @Column(name = "industry_type")
    private String industryType; //회사 특화필드

    @Column(name = "\"industryType1\"")
    private String industryType1; //회사 특화필드1

    @Column(name = "\"industryType2\"")
    private String industryType2; //회사 특화필드2

    @Column(name = "\"name\"")
    private String name; //조직명

    @Column(name = "\"orgId\"")
    private String orgId; //

    @Column(name = "phonenumber1")
    private String phoneNumber1;

    @Column(name = "phonenumber2")
    private String phoneNumber2;

    @Column(name = "products")
    private String products;

    @Column(name = "\"productLists\"")
    private String productLists;

    @Column(name = "\"productCategoryLists\"")
    private String productCategoryLists; //

    @Column(name = "regch")
    private String regCh; //대표자

    @Column(name = "representative")
    private String representative; //상태

    @Column(name = "state")
    private String state; //주

    @Column(name = "status")
    private String status; //상태

    @Column(name = "street_address")
    private String streetAddress; //도로명 주소

    @Column(name = "street_gl")
    private String streetGl; //글로벌 도로명 주소

    @Column(name = "\"type\"")
    private String type; //유형

    @Column(name = "vatno")
    private String vatNo; //회사 부가가치 번호

    @Column(name = "vendorcode")
    private String vendorCode; //회사 vendorcode

    @Column(name = "accountwebsite")
    private String accountWebsite; //웹사이트

    @Column(name = "zip_code")
    private String zipCode; //우편번호

    @Column(name = "created_at")
    private LocalDateTime createdAt; //생성일시

    @Column(name = "updated_at")
    private LocalDateTime updatedAt; //수정일시

    @Column(name = "bpid_in_cdc")
    private String bpidInCdc; //CDC bpID

    @Column(name = "\"mtLicense\"")
    private String mtLicense; //MT 라이센스

    @Column(name = "\"mtStartDate\"")
    private String mtStartDate; //MT 시작일

    @Column(name = "\"mtEndDate\"")
    private String mtEndDate; //MT 종료일

    @Column(name = "region")
    private String region; //지역

    @Column(name = "subsidiary")
    private String subsidiary; //법인코드

    // Getters and Setters

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

    public String getBizRegNo1() {
        return bizRegNo1;
    }

    public void setBizRegNo1(String bizRegNo1) {
        this.bizRegNo1 = bizRegNo1;
    }

    public String getBizRegNo02() {
        return bizRegNo02;
    }

    public void setBizRegNo02(String bizRegNo02) {
        this.bizRegNo02 = bizRegNo02;
    }

    public String getChannelType() {
        return channelType;
    }

    public void setChannelType(String channelType) {
        this.channelType = channelType;
    }

    public String getCisCode() {
        return cisCode;
    }

    public void setCisCode(String cisCode) {
        this.cisCode = cisCode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCityGl() {
        return cityGl;
    }

    public void setCityGl(String cityGl) {
        this.cityGl = cityGl;
    }

    public String getCompanyCode() {
        return companyCode;
    }

    public void setCompanyCode(String companyCode) {
        this.companyCode = companyCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDistrictGl() {
        return districtGl;
    }

    public void setDistrictGl(String districtGl) {
        this.districtGl = districtGl;
    }

    public String getDunsNo() {
        return dunsNo;
    }

    public void setDunsNo(String dunsNo) {
        this.dunsNo = dunsNo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNameGl() {
        return nameGl;
    }

    public void setNameGl(String nameGl) {
        this.nameGl = nameGl;
    }

    public String getFaxNo() {
        return faxNo;
    }

    public void setFaxNo(String faxNo) {
        this.faxNo = faxNo;
    }

    public String getIndustryType() {
        return industryType;
    }

    public void setIndustryType(String industryType) {
        this.industryType = industryType;
    }

    public String getIndustryType1() {
        return industryType1;
    }

    public void setIndustryType1(String industryType1) {
        this.industryType1 = industryType1;
    }

    public String getIndustryType2() {
        return industryType2;
    }

    public void setIndustryType2(String industryType2) {
        this.industryType2 = industryType2;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public String getPhoneNumber1() {
        return phoneNumber1;
    }

    public void setPhoneNumber1(String phoneNumber1) {
        this.phoneNumber1 = phoneNumber1;
    }

    public String getPhoneNumber2() {
        return phoneNumber2;
    }

    public void setPhoneNumber2(String phoneNumber2) {
        this.phoneNumber2 = phoneNumber2;
    }

    public String getProducts() {
        return products;
    }

    public void setProducts(String products) {
        this.products = products;
    }

    public String getProductLists() {
        return productLists;
    }

    public void setProductLists(String productLists) {
        this.productLists = productLists;
    }

    public String getProductCategoryLists() {
        return productCategoryLists;
    }

    public void setProductCategoryLists(String productCategoryLists) {
        this.productCategoryLists = productCategoryLists;
    }

    public String getRegCh() {
        return regCh;
    }

    public void setRegCh(String regCh) {
        this.regCh = regCh;
    }

    public String getRepresentative() {
        return representative;
    }

    public void setRepresentative(String representative) {
        this.representative = representative;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public void setStreetAddress(String streetAddress) {
        this.streetAddress = streetAddress;
    }

    public String getStreetGl() {
        return streetGl;
    }

    public void setStreetGl(String streetGl) {
        this.streetGl = streetGl;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getVatNo() {
        return vatNo;
    }

    public void setVatNo(String vatNo) {
        this.vatNo = vatNo;
    }

    public String getVendorCode() {
        return vendorCode;
    }

    public void setVendorCode(String vendorCode) {
        this.vendorCode = vendorCode;
    }

    public String getAccountWebsite() {
        return accountWebsite;
    }

    public void setAccountWebsite(String accountWebsite) {
        this.accountWebsite = accountWebsite;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
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

    public String getBpidInCdc() {
        return bpidInCdc;
    }

    public void setBpidInCdc(String bpidInCdc) {
        this.bpidInCdc = bpidInCdc;
    }

    public String getMtLicense() {
        return mtLicense;
    }

    public void setMtLicense(String mtLicense) {
        this.mtLicense = mtLicense;
    }

    public String getMtStartDate() {
        return mtStartDate;
    }

    public void setMtStartDate(String mtStartDate) {
        this.mtStartDate = mtStartDate;
    }

    public String getMtEndDate() {
        return mtEndDate;
    }

    public void setMtEndDate(String mtEndDate) {
        this.mtEndDate = mtEndDate;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getSubsidiary() {
        return subsidiary;
    }

    public void setSubsidiary(String subsidiary) {
        this.subsidiary = subsidiary;
    }
}