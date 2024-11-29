package com.samsung.ciam.models;

import java.sql.*;
import java.io.Serializable;

import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Setter;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.ToString;

@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor
@Builder
@ToString
@Setter
@Getter
@Entity
@Table(name = "revision_list")
public class RevisionList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // bigserial

    @Column(name = "revision_title")
    private String revisionTitle;

    @Column(name = "revision_contents")
    private String revisionContents;

    @Column(name = "language_id")
    private String languageId;

    @Column(name = "status")
    private String Status;

    @Column(name = "kind")
    private String Kind;

    @Column(name = "apply_at")
    private Timestamp applyAt;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @Column(name = "coverage")
    private String Coverage;

    @Column(name = "location")
    private String Location;

    @Column(name = "subsidiary")
    private String Subsidiary;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRevisionTitle() {
        return revisionTitle;
    }

    public void setRevisionTitle(String revisionTitle) {
        this.revisionTitle = revisionTitle;
    }

    public String getRevisionContents() {
        return revisionContents;
    }

    public void setRevisionContents(String revisionContents) {
        this.revisionContents = revisionContents;
    }

    public String getLanguageId() {
        return languageId;
    }

    public void setLanguageId(String languageId) {
        this.languageId = languageId;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public String getKind() {
        return Kind;
    }

    public void setKind(String kind) {
        Kind = kind;
    }

    public Timestamp getApplyAt() {
        return applyAt;
    }

    public void setApplyAt(Timestamp applyAt) {
        this.applyAt = applyAt;
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

    public String getCoverage() {
        return Coverage;
    }

    public void setCoverage(String coverage) {
        Coverage = coverage;
    }

    public String getLocation() {
        return Location;
    }

    public void setLocation(String location) {
        Location = location;
    }

    public String getSubsidiary() {
        return Subsidiary;
    }

    public void setSubsidiary(String subsidiary) {
        Subsidiary = subsidiary;
    }
}
