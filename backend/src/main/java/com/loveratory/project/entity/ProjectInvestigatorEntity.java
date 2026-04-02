package com.loveratory.project.entity;

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
 * 專案主持人 Entity。
 * 對應資料表 project_investigators，儲存專案與主持人的關聯。
 */
@Entity
@Getter
@Setter
@FieldNameConstants
@Table(name = "project_investigators")
public class ProjectInvestigatorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "added_by", nullable = false)
    private UUID addedBy;

    @Column(name = "added_at", nullable = false, updatable = false)
    private ZonedDateTime addedAt = ZonedDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ProjectInvestigatorStatus status;
}
