package com.loveratory.experiment.entity;

import com.loveratory.experiment.dto.internal.FormConfig;
import com.loveratory.experiment.dto.internal.NotificationConfig;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * 實驗 Entity。
 * 對應資料表 experiments，儲存實驗主檔資料。
 */
@Entity
@Getter
@Setter
@FieldNameConstants
@Table(name = "experiments")
@EntityListeners(AuditingEntityListener.class)
public class ExperimentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "location")
    private String location;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Column(name = "max_participants_per_slot", nullable = false)
    private Integer maxParticipantsPerSlot;

    @Column(name = "slug", nullable = false, unique = true)
    private String slug;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ExperimentStatus status;

    @Column(name = "allow_duplicate_email")
    private Boolean allowDuplicateEmail;

    @Column(name = "allow_participant_cancel")
    private Boolean allowParticipantCancel;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "form_config", columnDefinition = "jsonb")
    private FormConfig formConfig;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "notification_config", columnDefinition = "jsonb")
    private NotificationConfig notificationConfig;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;
}
