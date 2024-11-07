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

/**
 * 1. 파일명   : UserOnboardHistory.java
 * 2. 패키지   : com.samsung.ciam.models
 * 3. 설명     : 가입시 CDC 조회 이력 저장 테이블 (JPA)
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
    private Long id; //고유 식별자

    @Column(name = "onboard_type")
    private String onboardType; //가입유형

    @Column(name = "cdc_uid")
    private String cdcUid; //CDC_UID

    @Column
    private String email;  //이메일

    @Column
    private String channel; //채널

    @Column(name = "is_employee")
    private boolean isEmployee; //직원여부

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "cdc_data")
    private String cdcData;  // CDC이력 JSON 데이터

    @Column(name = "validation_status")
    private String validationStatus; //조회 응답 상태

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "validation_detail")
    private String validationDetail;  //조회 응답 상세

    @Column(name = "pending_approval")
    private boolean pendingApproval; //승인상태

    @Column(name = "is_retry_required")
    private boolean isRetryRequired; //재시도 필수 여부

    @Column(name = "created_at")
    private LocalDateTime createdAt; //등록일시

    @Column(name = "updated_at")
    private LocalDateTime updatedAt; //수정일시

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