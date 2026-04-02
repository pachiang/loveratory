package com.loveratory.lab.usecase;

import com.loveratory.auth.entity.UserEntity;
import com.loveratory.auth.entity.UserRole;
import com.loveratory.auth.manager.UserManager;
import com.loveratory.common.exception.BusinessException;
import com.loveratory.common.exception.ErrorCode;
import com.loveratory.common.util.SecurityUtil;
import com.loveratory.lab.dto.request.LabCreateRequest;
import com.loveratory.lab.dto.response.LabDetailResponse;
import com.loveratory.lab.dto.response.LabSummaryResponse;
import com.loveratory.lab.entity.LabEntity;
import com.loveratory.lab.entity.LabMemberEntity;
import com.loveratory.lab.entity.LabStatus;
import com.loveratory.lab.manager.LabManager;
import com.loveratory.lab.manager.LabMemberManager;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * 實驗室業務邏輯。
 * 處理實驗室的建立、重新申請、查詢等操作。
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class LabUseCase {

    private final LabManager labManager;
    private final LabMemberManager labMemberManager;
    private final UserManager userManager;

    /**
     * 建立實驗室申請。
     * 建立後狀態為 PENDING，需等待系統管理員審核。
     *
     * @param request 建立實驗室請求
     * @return 實驗室詳情回應
     */
    @Transactional(rollbackFor = Exception.class)
    public LabDetailResponse createLab(@NonNull LabCreateRequest request) {
        UUID currentUserId = SecurityUtil.getCurrentUserId();

        if (labManager.existsByCode(request.getCode())) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "實驗室代碼已被使用");
        }

        LabEntity labEntity = new LabEntity();
        labEntity.setName(request.getName());
        labEntity.setCode(request.getCode());
        labEntity.setDescription(request.getDescription());
        labEntity.setStatus(LabStatus.PENDING);
        labEntity.setAppliedBy(currentUserId);

        LabEntity savedLabEntity = labManager.save(labEntity);

        UserEntity applicant = userManager.findByIdOrThrow(currentUserId);
        return LabDetailResponse.fromEntity(savedLabEntity, applicant.getName());
    }

    /**
     * 重新申請實驗室。
     * 僅限被拒絕的申請人修改內容後重新提交。
     *
     * @param labId   實驗室 ID
     * @param request 建立實驗室請求
     * @return 實驗室詳情回應
     */
    @Transactional(rollbackFor = Exception.class)
    public LabDetailResponse reapplyLab(@NonNull UUID labId, @NonNull LabCreateRequest request) {
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        LabEntity labEntity = labManager.findByIdOrThrow(labId);

        if (!labEntity.getAppliedBy().equals(currentUserId)) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        if (labEntity.getStatus() != LabStatus.REJECTED) {
            throw new BusinessException(ErrorCode.INVALID_LAB_STATUS);
        }

        if (!labEntity.getCode().equals(request.getCode()) && labManager.existsByCode(request.getCode())) {
            throw new BusinessException(ErrorCode.INVALID_PARAMETER, "實驗室代碼已被使用");
        }

        labEntity.setName(request.getName());
        labEntity.setCode(request.getCode());
        labEntity.setDescription(request.getDescription());
        labEntity.setStatus(LabStatus.PENDING);
        labEntity.setReviewedBy(null);
        labEntity.setReviewNote(null);
        labEntity.setApprovedAt(null);

        LabEntity savedLabEntity = labManager.save(labEntity);

        UserEntity applicant = userManager.findByIdOrThrow(currentUserId);
        return LabDetailResponse.fromEntity(savedLabEntity, applicant.getName());
    }

    /**
     * 查詢當前使用者所屬的實驗室列表。
     *
     * @return 實驗室摘要列表
     */
    @Transactional(readOnly = true)
    public List<LabSummaryResponse> findMyLabs() {
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        List<LabMemberEntity> memberships = labMemberManager.findLabsByUserId(currentUserId);

        return memberships.stream()
                .map(membership -> {
                    LabEntity labEntity = labManager.findByIdOrThrow(membership.getLabId());
                    return LabSummaryResponse.of(labEntity, membership.getRole());
                })
                .toList();
    }

    /**
     * 查詢實驗室詳情。
     * 僅限實驗室啟用中成員或系統管理員查看。
     *
     * @param labId 實驗室 ID
     * @return 實驗室詳情回應
     */
    @Transactional(readOnly = true)
    public LabDetailResponse findLabDetail(@NonNull UUID labId) {
        UUID currentUserId = SecurityUtil.getCurrentUserId();
        LabEntity labEntity = labManager.findByIdOrThrow(labId);

        UserEntity currentUser = userManager.findByIdOrThrow(currentUserId);
        boolean isSystemAdmin = currentUser.getRole() == UserRole.SYSTEM_ADMIN;
        boolean isActiveMember = labMemberManager.existsActiveMember(labId, currentUserId);

        if (!isSystemAdmin && !isActiveMember) {
            throw new BusinessException(ErrorCode.ACCESS_DENIED);
        }

        UserEntity applicant = userManager.findByIdOrThrow(labEntity.getAppliedBy());
        return LabDetailResponse.fromEntity(labEntity, applicant.getName());
    }
}
