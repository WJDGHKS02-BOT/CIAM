package com.samsung.ciam.models;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * 1. 파일명   : users.java
 * 2. 패키지   : com.samsung.ciam.models
 * 3. 설명     : 로그인 사용자 관리 테이블 (JPA)
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
@Table(name = "users")
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; //고유 식별자

    @Column(name = "cdc_uid", nullable = false)
    private String cdcUid; //CDC_UID

    @Column(name = "email", nullable = false)
    private String email; //로그인 이메일

    @Column(name = "created_at")
    private LocalDateTime createdAt; //등록일시

    @Column(name = "updated_at")
    private LocalDateTime updatedAt; //수정일시

    @Column(name = "username")
    private String username; //로그인ID

    @Column(name = "password")
    private String password; //패스워드

   
    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCdcUid() {
        return cdcUid;
    }

    public void setCdcUid(String cdcUid) {
        this.cdcUid = cdcUid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
