package com.loveratory.experiment.controller;

import com.loveratory.experiment.dto.internal.FormConfig;
import com.loveratory.experiment.dto.internal.NotificationConfig;
import com.loveratory.experiment.dto.request.ExperimentCreateRequest;
import com.loveratory.experiment.dto.request.ExperimentStatusUpdateRequest;
import com.loveratory.experiment.dto.request.ExperimentUpdateRequest;
import com.loveratory.experiment.dto.response.ExperimentDetailResponse;
import com.loveratory.experiment.dto.response.ExperimentSummaryResponse;
import com.loveratory.experiment.usecase.ExperimentUseCase;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * 實驗管理 API。
 * 提供實驗的建立、查詢、更新、狀態變更與設定管理功能。
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@Tag(name = "實驗管理", description = "實驗建立、查詢、更新與設定管理")
public class ExperimentController {

    private final ExperimentUseCase experimentUseCase;

    /**
     * 建立實驗。
     *
     * @param projectId 專案 ID
     * @param request   建立實驗請求
     * @return 實驗詳情回應
     */
    @Operation(summary = "建立實驗", description = "在指定專案下建立新實驗，狀態為 DRAFT")
    @PostMapping("/api/projects/{projectId}/experiments")
    public ExperimentDetailResponse createExperiment(
            @Parameter(description = "專案 ID") @PathVariable UUID projectId,
            @Valid @RequestBody ExperimentCreateRequest request) {
        log.info("建立實驗，projectId: {}, name: {}, slug: {}", projectId, request.getName(), request.getSlug());
        return experimentUseCase.createExperiment(projectId, request);
    }

    /**
     * 查詢專案下的所有實驗列表。
     *
     * @param projectId 專案 ID
     * @return 實驗摘要列表
     */
    @Operation(summary = "查詢實驗列表", description = "列出指定專案下的所有實驗")
    @GetMapping("/api/projects/{projectId}/experiments")
    public List<ExperimentSummaryResponse> findExperiments(
            @Parameter(description = "專案 ID") @PathVariable UUID projectId) {
        log.info("查詢實驗列表，projectId: {}", projectId);
        return experimentUseCase.findExperiments(projectId);
    }

    /**
     * 查詢實驗詳情。
     *
     * @param experimentId 實驗 ID
     * @return 實驗詳情回應
     */
    @Operation(summary = "查詢實驗詳情", description = "根據實驗 ID 查詢完整資訊")
    @GetMapping("/api/experiments/{experimentId}")
    public ExperimentDetailResponse findExperimentDetail(
            @Parameter(description = "實驗 ID") @PathVariable UUID experimentId) {
        log.info("查詢實驗詳情，experimentId: {}", experimentId);
        return experimentUseCase.findExperimentDetail(experimentId);
    }

    /**
     * 更新實驗資訊。
     *
     * @param experimentId 實驗 ID
     * @param request      更新實驗請求
     * @return 實驗詳情回應
     */
    @Operation(summary = "更新實驗", description = "更新實驗基本資訊")
    @PutMapping("/api/experiments/{experimentId}")
    public ExperimentDetailResponse updateExperiment(
            @Parameter(description = "實驗 ID") @PathVariable UUID experimentId,
            @Valid @RequestBody ExperimentUpdateRequest request) {
        log.info("更新實驗，experimentId: {}", experimentId);
        return experimentUseCase.updateExperiment(experimentId, request);
    }

    /**
     * 更新實驗狀態。
     *
     * @param experimentId 實驗 ID
     * @param request      更新狀態請求
     * @return 實驗詳情回應
     */
    @Operation(summary = "更新實驗狀態", description = "變更實驗狀態（DRAFT / OPEN / CLOSED / ARCHIVED）")
    @PutMapping("/api/experiments/{experimentId}/status")
    public ExperimentDetailResponse updateExperimentStatus(
            @Parameter(description = "實驗 ID") @PathVariable UUID experimentId,
            @Valid @RequestBody ExperimentStatusUpdateRequest request) {
        log.info("更新實驗狀態，experimentId: {}, status: {}", experimentId, request.getStatus());
        return experimentUseCase.updateExperimentStatus(experimentId, request);
    }

    /**
     * 更新報名表單設定。
     *
     * @param experimentId 實驗 ID
     * @param formConfig   表單設定
     * @return 實驗詳情回應
     */
    @Operation(summary = "更新報名表單設定", description = "更新實驗的報名表單欄位設定（Email 欄位不可關閉）")
    @PutMapping("/api/experiments/{experimentId}/form-config")
    public ExperimentDetailResponse updateFormConfig(
            @Parameter(description = "實驗 ID") @PathVariable UUID experimentId,
            @Valid @RequestBody FormConfig formConfig) {
        log.info("更新報名表單設定，experimentId: {}", experimentId);
        return experimentUseCase.updateFormConfig(experimentId, formConfig);
    }

    /**
     * 更新通知設定。
     *
     * @param experimentId       實驗 ID
     * @param notificationConfig 通知設定
     * @return 實驗詳情回應
     */
    @Operation(summary = "更新通知設定", description = "更新實驗的通知設定")
    @PutMapping("/api/experiments/{experimentId}/notification-config")
    public ExperimentDetailResponse updateNotificationConfig(
            @Parameter(description = "實驗 ID") @PathVariable UUID experimentId,
            @Valid @RequestBody NotificationConfig notificationConfig) {
        log.info("更新通知設定，experimentId: {}", experimentId);
        return experimentUseCase.updateNotificationConfig(experimentId, notificationConfig);
    }
}
