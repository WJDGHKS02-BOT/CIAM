package com.samsung.ciam.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;

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
@Table(name = "authorized_subsidiaries")
public class AuthorizedSubsidiary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "channel")
    private String channel;

    @Column(name = "cdc_uid")
    private String cdcUid;

    @Column(name = "authorized_subsidiary_list")
    private String authorizedSubsidiaryList;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}