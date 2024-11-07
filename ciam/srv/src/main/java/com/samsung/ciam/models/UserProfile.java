package com.samsung.ciam.models;

/**
 * 1. 파일명   : UserProfile.java
 * 2. 패키지   : com.samsung.ciam.models
 * 3. 설명     : 유저관리
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
public class UserProfile {

    private String cdc_uid; // cdc_uid만 기존 소스처럼 형태 유지

    private String firstName;

    private String lastName;

    private String email;
    
    private String languages;

    private String countryCode;

    private String city;

    private String state;

    private String workPhoneCode;

    private String workPhone;
    
    private String mobilePhoneCode;

    private String mobilePhone;

    private String jobtitle;

    private String userDepartment;

    private String salutation;

    private String username;

    private String locale;

    public String getCdc_uid() {
        return cdc_uid;
    }

    public void setCdc_uid(String cdc_uid) {
        this.cdc_uid = cdc_uid;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLanguages() {
        return languages;
    }

    public void setLanguages(String languages) {
        this.languages = languages;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
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

    public String getWorkPhoneCode() {
        return workPhoneCode;
    }

    public void setWorkPhoneCode(String workPhoneCode) {
        this.workPhoneCode = workPhoneCode;
    }

    public String getWorkPhone() {
        return workPhone;
    }

    public void setWorkPhone(String workPhone) {
        this.workPhone = workPhone;
    }

    public String getMobilePhoneCode() {
        return mobilePhoneCode;
    }

    public void setMobilePhoneCode(String mobilePhoneCode) {
        this.mobilePhoneCode = mobilePhoneCode;
    }

    public String getMobilePhone() {
        return mobilePhone;
    }

    public void setMobilePhone(String mobilePhone) {
        this.mobilePhone = mobilePhone;
    }

    public String getJobtitle() {
        return jobtitle;
    }

    public void setJobtitle(String jobtitle) {
        this.jobtitle = jobtitle;
    }

    public String getUserDepartment() {
        return userDepartment;
    }

    public void setUserDepartment(String userDepartment) {
        this.userDepartment = userDepartment;
    }

    public String getSalutation() {
        return salutation;
    }

    public void setSalutation(String salutation) {
        this.salutation = salutation;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }
}