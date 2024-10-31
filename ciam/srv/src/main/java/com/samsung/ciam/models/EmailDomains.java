package com.samsung.ciam.models;

import jakarta.persistence.*;

@Entity
@Table(name = "email_domains")
public class EmailDomains {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "domain", nullable = false)
    private String domain;

    @Column(name = "type")
    private String type;

    @Column(name = "bpid")
    private String bpid;

    @Column(name = "uid" , nullable = false)
    private String uid;

    @Column(name = "created_at")
    private String createAt;

    @Column(name = "updated_at")
    private String updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getBpid() {
        return bpid;
    }

    public void setBpid(String bpid) {
        this.bpid = bpid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getCreateAt() {
        return createAt;
    }

    public void setCreateAt(String createAt) {
        this.createAt = createAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
