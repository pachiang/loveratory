package com.loveratory.lab.controller;

import com.loveratory.lab.dto.request.LabCreateRequest;
import com.loveratory.lab.dto.request.LabUpdateRequest;
import com.loveratory.lab.dto.response.LabDetailResponse;
import com.loveratory.lab.dto.response.LabSummaryResponse;
import com.loveratory.lab.usecase.LabUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * 實驗室管理 API。
 * 提供實驗室的建立、重新申請與查詢功能。
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/labs")
@Tag(name = "實驗室管理", description = "實驗室建立、重新申請與查詢")
public class LabController {

    private final LabUseCase labUseCase;

    /**
     * 申請建立實驗室。
     *
     * @param request 建立實驗室請求
     * @return 實驗室詳情回應
     */
    @Operation(summary = "申請建立實驗室", description = "提交建立實驗室申請，狀態為 PENDING 待審核")
    @PostMapping
    public LabDetailResponse createLab(@Valid @RequestBody LabCreateRequest request) {
        log.info("申請建立實驗室，name: {}, code: {}", request.getName(), request.getCode());
        return labUseCase.createLab(request);
    }

    /**
     * 重新申請實驗室。
     *
     * @param labId   實驗室 ID
     * @param request 建立實驗室請求
     * @return 實驗室詳情回應
     */
    @Operation(summary = "重新申請實驗室", description = "被拒絕後修改內容重新提交申請")
    @PostMapping("/{labId}/reapply")
    public LabDetailResponse reapplyLab(
            @Parameter(description = "實驗室 ID") @PathVariable UUID labId,
            @Valid @RequestBody LabCreateRequest request) {
        log.info("重新申請實驗室，labId: {}, name: {}, code: {}", labId, request.getName(), request.getCode());
        return labUseCase.reapplyLab(labId, request);
    }

    /**
     * 查詢我所屬的實驗室列表。
     *
     * @return 實驗室摘要列表
     */
    @Operation(summary = "查詢我的實驗室", description = "列出當前使用者所屬的所有實驗室")
    @GetMapping
    public List<LabSummaryResponse> findMyLabs() {
        log.info("查詢我的實驗室列表");
        return labUseCase.findMyLabs();
    }

    /**
     * 查詢實驗室詳情。
     *
     * @param labId 實驗室 ID
     * @return 實驗室詳情回應
     */
    @Operation(summary = "查詢實驗室詳情", description = "根據實驗室 ID 查詢完整資訊")
    @GetMapping("/{labId}")
    public LabDetailResponse findLabDetail(
            @Parameter(description = "實驗室 ID") @PathVariable UUID labId) {
        log.info("查詢實驗室詳情，labId: {}", labId);
        return labUseCase.findLabDetail(labId);
    }

    /**
     * 更新實驗室資訊。
     *
     * @param labId 實驗室 ID
     * @param request 更新實驗室請求
     * @return 實驗室詳情回應
     */
    @Operation(summary = "更新實驗室資訊", description = "僅限實驗室管理員更新實驗室名稱與描述")
    @PutMapping("/{labId}")
    public LabDetailResponse updateLab(
            @Parameter(description = "實驗室 ID") @PathVariable UUID labId,
            @Valid @RequestBody LabUpdateRequest request) {
        log.info("更新實驗室資訊，labId: {}, name: {}", labId, request.getName());
        return labUseCase.updateLab(labId, request);
    }
}
