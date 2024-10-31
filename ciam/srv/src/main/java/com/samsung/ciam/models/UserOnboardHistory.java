package com.samsung.ciam.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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
@Table(name = "user_onboard_history")
public class UserOnboardHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "onboard_type")
    private String onboardType;

    @Column(name = "cdc_uid")
    private String cdcUid;

    @Column
    private String email;

    @Column
    private String channel;

    @Column(name = "is_employee")
    private boolean isEmployee;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "cdc_data")
    private String cdcData;  // Json data

    @Column(name = "validation_status")
    private String validationStatus;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "validation_detail")
    private String validationDetail;  //Json data

    @Column(name = "pending_approval")
    private boolean pendingApproval;

    @Column(name = "is_retry_required")
    private boolean isRetryRequired;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public UserOnboardHistory(
        String onboardType,
        String cdcUid,
        String email,
        String channel,
        Boolean isEmployee,
        String cdcData,
        Boolean pendingApproval,
        String validationStatus,
        String validationDetail,
        Boolean isRetryRequired
    ){
        this.onboardType = onboardType;
        this.cdcUid = cdcUid;
        this.email = email;
        this.channel = channel;
        this.isEmployee = isEmployee;
        this.cdcData = cdcData;
        this.pendingApproval = pendingApproval;
        this.validationStatus = validationStatus;
        this.validationDetail = validationDetail;
        this.isRetryRequired = isRetryRequired;
        this.createdAt = LocalDateTime.now();
        this.updatedAt =  LocalDateTime.now();
    }
}