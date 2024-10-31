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
@Table(name = "btp_audit_log")
public class BtpAuditLog implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;  // bigserial

    @Column(name = "requester_uid")
    private String requesterUid;

    @Column
    private String channel;

    @Column
    private String type;

    @Column(name = "menu_type")
    private String menuType;

    @Column
    private String action;

    @Column(name = "result_count")
    private Integer resultCount;

    @Column
    private String items;

    @Column
    private String condition;

    @Column
    private String reason;

    @Column(name = "created_at")
    private Timestamp createdAt;

    @Column(name = "updated_at")
    private Timestamp updatedAt;

}
