package com.loveratory.admin.controller;

import com.loveratory.admin.usecase.AdminLabUseCase;
import com.loveratory.lab.dto.request.LabRejectRequest;
import com.loveratory.lab.dto.response.LabDetailResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * 系統管理 - 實驗室審核 API。
 * 提供實驗室申請的審核與查詢功能，僅限系統管理員使用。
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/admin/labs")
@Tag(name = "系統管理 - 實驗室審核", description = "實驗室申請審核與管理")
public class AdminLabController {

    private final AdminLabUseCase adminLabUseCase;

    /**
     * 查詢待審核的實驗室申請列表。
     *
     * @param pageable 分頁參數
     * @return 待審核實驗室分頁結果
     */
    @Operation(summary = "查詢待審核實驗室", description = "列出所有待審核的實驗室申請")
    @GetMapping("/pending")
    public Page<LabDetailResponse> findPendingLabs(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        log.info("查詢待審核實驗室列表，page: {}", pageable);
        return adminLabUseCase.findPendingLabs(pageable);
    }

    /**
     * 核准實驗室申請。
     *
     * @param labId 實驗室 ID
     * @return 實驗室詳情回應
     */
    @Operation(summary = "核准實驗室", description = "核准實驗室申請，申請人自動成為管理員")
    @PutMapping("/{labId}/approve")
    public LabDetailResponse approveLab(
            @Parameter(description = "實驗室 ID") @PathVariable UUID labId) {
        log.info("核准實驗室申請，labId: {}", labId);
        return adminLabUseCase.approveLab(labId);
    }

    /**
     * 拒絕實驗室申請。
     *
     * @param labId   實驗室 ID
     * @param request 拒絕請求（含拒絕原因）
     * @return 實驗室詳情回應
     */
    @Operation(summary = "拒絕實驗室", description = "拒絕實驗室申請並填寫拒絕原因")
    @PutMapping("/{labId}/reject")
    public LabDetailResponse rejectLab(
            @Parameter(description = "實驗室 ID") @PathVariable UUID labId,
            @Valid @RequestBody LabRejectRequest request) {
        log.info("拒絕實驗室申請，labId: {}", labId);
        return adminLabUseCase.rejectLab(labId, request);
    }

    /**
     * 查詢所有實驗室列表。
     *
     * @param pageable 分頁參數
     * @return 實驗室分頁結果
     */
    @Operation(summary = "查詢所有實驗室", description = "列出系統中所有實驗室")
    @GetMapping
    public Page<LabDetailResponse> findAllLabs(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {
        log.info("查詢所有實驗室列表，page: {}", pageable);
        return adminLabUseCase.findAllLabs(pageable);
    }
}
