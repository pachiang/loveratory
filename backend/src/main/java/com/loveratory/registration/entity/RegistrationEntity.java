package com.loveratory.registration.entity;

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
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * 報名 Entity。
 * 對應資料表 registrations，儲存受試者報名資料。
 */
@Entity
@Getter
@Setter
@FieldNameConstants
@Table(name = "registrations")
@EntityListeners(AuditingEntityListener.class)
public class RegistrationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "time_slot_id", nullable = false)
    private UUID timeSlotId;

    @Column(name = "participant_email", nullable = false)
    private String participantEmail;

    @Column(name = "participant_name")
    private String participantName;

    @Column(name = "participant_phone")
    private String participantPhone;

    @Column(name = "participant_student_id")
    private String participantStudentId;

    @Column(name = "participant_age")
    private Integer participantAge;

    @Column(name = "participant_gender")
    private String participantGender;

    @Column(name = "participant_dominant_hand")
    private String participantDominantHand;

    @Column(name = "participant_notes", columnDefinition = "TEXT")
    private String participantNotes;

    @Column(name = "cancel_token", nullable = false, unique = true)
    private String cancelToken;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RegistrationStatus status;

    @Column(name = "registered_at", nullable = false, updatable = false)
    private ZonedDateTime registeredAt;

    @Column(name = "cancelled_at")
    private ZonedDateTime cancelledAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;
}
