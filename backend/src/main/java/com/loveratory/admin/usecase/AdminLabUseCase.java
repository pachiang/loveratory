package com.loveratory.admin.usecase;

import com.loveratory.auth.entity.UserEntity;
import com.loveratory.auth.manager.UserManager;
import com.loveratory.common.exception.BusinessException;
import com.loveratory.common.exception.ErrorCode;
import com.loveratory.common.util.SecurityUtil;
import com.loveratory.lab.dto.request.LabRejectRequest;
import com.loveratory.lab.dto.response.LabDetailResponse;
import com.loveratory.lab.entity.LabEntity;
import com.loveratory.lab.entity.LabMemberEntity;
import com.loveratory.lab.entity.LabMemberRole;
import com.loveratory.lab.entity.LabMemberStatus;
import com.loveratory.lab.entity.LabStatus;
import com.loveratory.lab.manager.LabManager;
import com.loveratory.lab.manager.LabMemberManager;
import com.loveratory.lab.specification.LabSpecification;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * 系統管理員實驗室審核業務邏輯。
 * 處理實驗室申請的審核、查詢等操作。
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class AdminLabUseCase {

    private final LabManager labManager;
    private final LabMemberManager labMemberManager;
    private final UserManager userManager;

    /**
     * 查詢待審核的實驗室申請列表。
     *
     * @param pageable 分頁參數
     * @return 待審核實驗室分頁結果
     */
    @Transactional(readOnly = true)
    public Page<LabDetailResponse> findPendingLabs(@NonNull Pageable pageable) {
        Specification<LabEntity> specification = LabSpecification.hasStatus(LabStatus.PENDING);
        Page<LabEntity> labPage = labManager.findAll(specification, pageable);

        return labPage.map(labEntity -> {
            UserEntity applicant = userManager.findByIdOrThrow(labEntity.getAppliedBy());
            return LabDetailResponse.fromEntity(labEntity, applicant.getName());
        });
    }

    /**
     * 核准實驗室申請。
     * 核准後申請人自動成為實驗室管理員。
     *
     * @param labId 實驗室 ID
     * @return 實驗室詳情回應
     */
    @Transactional(rollbackFor = Exception.class)
    public LabDetailResponse approveLab(@NonNull UUID labId) {
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        LabEntity labEntity = labManager.findByIdOrThrow(labId);

        if (labEntity.getStatus() != LabStatus.PENDING) {
            throw new BusinessException(ErrorCode.INVALID_LAB_STATUS);
        }

        labEntity.setStatus(LabStatus.APPROVED);
        labEntity.setReviewedBy(currentUserId);
        labEntity.setApprovedAt(ZonedDateTime.now());
        LabEntity savedLabEntity = labManager.save(labEntity);

        // 申請人自動成為實驗室管理員
        LabMemberEntity memberEntity = new LabMemberEntity();
        memberEntity.setLabId(labId);
        memberEntity.setUserId(labEntity.getAppliedBy());
        memberEntity.setRole(LabMemberRole.LAB_ADMIN);
        memberEntity.setStatus(LabMemberStatus.ACTIVE);
        labMemberManager.save(memberEntity);

        UserEntity applicant = userManager.findByIdOrThrow(labEntity.getAppliedBy());
        return LabDetailResponse.fromEntity(savedLabEntity, applicant.getName());
    }

    /**
     * 拒絕實驗室申請。
     *
     * @param labId   實驗室 ID
     * @param request 拒絕請求（含拒絕原因）
     * @return 實驗室詳情回應
     */
    @Transactional(rollbackFor = Exception.class)
    public LabDetailResponse rejectLab(@NonNull UUID labId, @NonNull LabRejectRequest request) {
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        LabEntity labEntity = labManager.findByIdOrThrow(labId);

        if (labEntity.getStatus() != LabStatus.PENDING) {
            throw new BusinessException(ErrorCode.INVALID_LAB_STATUS);
        }

        labEntity.setStatus(LabStatus.REJECTED);
        labEntity.setReviewedBy(currentUserId);
        labEntity.setReviewNote(request.getReviewNote());
        LabEntity savedLabEntity = labManager.save(labEntity);

        UserEntity applicant = userManager.findByIdOrThrow(labEntity.getAppliedBy());
        return LabDetailResponse.fromEntity(savedLabEntity, applicant.getName());
    }

    /**
     * 查詢所有實驗室列表（分頁）。
     *
     * @param pageable 分頁參數
     * @return 實驗室分頁結果
     */
    @Transactional(readOnly = true)
    public Page<LabDetailResponse> findAllLabs(@NonNull Pageable pageable) {
        Specification<LabEntity> specification = Specification.where(null);
        Page<LabEntity> labPage = labManager.findAll(specification, pageable);

        return labPage.map(labEntity -> {
            UserEntity applicant = userManager.findByIdOrThrow(labEntity.getAppliedBy());
            return LabDetailResponse.fromEntity(labEntity, applicant.getName());
        });
    }
}
