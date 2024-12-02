package com.samsung.ciam.models;

import java.sql.*;

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
@Table(name = "revision_notice")
public class RevisionNotice {

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

    @Column(name = "type")
    private String type;

    @Column(name = "apply_at")
    private Timestamp applyAt;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

    @Column(name = "channel")
    private String Channel;

    @Column(name = "subsidiary")
    private String Subsidiary;

    public Long getId() {
        return id;
    }

    public String getRevisionTitle() {
        return revisionTitle;
    }

    public String getRevisionContents() {
        return revisionContents;
    }

    public String getLanguageId() {
        return languageId;
    }

    public String getStatus() {
        return Status;
    }

    public String getType() {
        return type;
    }

    public Timestamp getApplyAt() {
        return applyAt;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public Timestamp getUpdatedAt() {
        return updatedAt;
    }

    public String getChannel() {
        return Channel;
    }

    public String getSubsidiary() {
        return Subsidiary;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setRevisionTitle(String revisionTitle) {
        this.revisionTitle = revisionTitle;
    }

    public void setRevisionContents(String revisionContents) {
        this.revisionContents = revisionContents;
    }

    public void setLanguageId(String languageId) {
        this.languageId = languageId;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setApplyAt(Timestamp applyAt) {
        this.applyAt = applyAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setChannel(String channel) {
        Channel = channel;
    }

    public void setSubsidiary(String subsidiary) {
        Subsidiary = subsidiary;
    }
}
