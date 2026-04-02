package com.loveratory.lab.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * 實驗室成員 Entity。
 * 對應資料表 lab_members，儲存實驗室與使用者的成員關聯。
 */
@Entity
@Getter
@Setter
@FieldNameConstants
@Table(name = "lab_members")
public class LabMemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "lab_id", nullable = false)
    private UUID labId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private LabMemberRole role;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private ZonedDateTime joinedAt = ZonedDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private LabMemberStatus status;
}
