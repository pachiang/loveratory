package com.loveratory.slot.controller;

import com.loveratory.slot.dto.request.SlotBatchCreateRequest;
import com.loveratory.slot.dto.request.SlotCreateRequest;
import com.loveratory.slot.dto.request.SlotUpdateRequest;
import com.loveratory.slot.dto.response.TimeSlotResponse;
import com.loveratory.slot.usecase.TimeSlotUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
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
 * 時段管理 API。
 * 提供時段的建立、查詢、更新與取消功能。
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1")
@Tag(name = "時段管理", description = "時段建立、查詢、更新與取消")
public class TimeSlotController {

    private final TimeSlotUseCase timeSlotUseCase;

    /**
     * 建立時段。
     *
     * @param experimentId 實驗 ID
     * @param requests     建立時段請求列表
     * @return 時段回應列表
     */
    @Operation(summary = "建立時段", description = "為指定實驗建立一個或多個時段")
    @PostMapping("/experiments/{experimentId}/slots")
    public List<TimeSlotResponse> createSlots(
            @Parameter(description = "實驗 ID") @PathVariable UUID experimentId,
            @Valid @RequestBody List<SlotCreateRequest> requests) {
        log.info("建立時段，experimentId: {}, count: {}", experimentId, requests.size());
        return timeSlotUseCase.createSlots(experimentId, requests);
    }

    /**
     * 批次建立時段。
     *
     * @param experimentId 實驗 ID
     * @param request      批次建立時段請求
     * @return 時段回應列表
     */
    @Operation(summary = "批次建立時段", description = "根據日期範圍、星期與時段區間自動產生多個時段")
    @PostMapping("/experiments/{experimentId}/slots/batch")
    public List<TimeSlotResponse> createSlotsBatch(
            @Parameter(description = "實驗 ID") @PathVariable UUID experimentId,
            @Valid @RequestBody SlotBatchCreateRequest request) {
        log.info("批次建立時段，experimentId: {}, startDate: {}, endDate: {}",
                experimentId, request.getStartDate(), request.getEndDate());
        return timeSlotUseCase.createSlotsBatch(experimentId, request);
    }

    /**
     * 查詢實驗的所有時段。
     *
     * @param experimentId 實驗 ID
     * @return 時段回應列表
     */
    @Operation(summary = "查詢時段列表", description = "列出指定實驗的所有時段")
    @GetMapping("/experiments/{experimentId}/slots")
    public List<TimeSlotResponse> findSlots(
            @Parameter(description = "實驗 ID") @PathVariable UUID experimentId) {
        log.info("查詢時段列表，experimentId: {}", experimentId);
        return timeSlotUseCase.findSlots(experimentId);
    }

    /**
     * 更新時段。
     *
     * @param slotId  時段 ID
     * @param request 更新時段請求
     * @return 時段回應
     */
    @Operation(summary = "更新時段", description = "更新指定時段的時間與容量")
    @PutMapping("/slots/{slotId}")
    public TimeSlotResponse updateSlot(
            @Parameter(description = "時段 ID") @PathVariable UUID slotId,
            @Valid @RequestBody SlotUpdateRequest request) {
        log.info("更新時段，slotId: {}", slotId);
        return timeSlotUseCase.updateSlot(slotId, request);
    }

    /**
     * 取消時段。
     *
     * @param slotId 時段 ID
     */
    @Operation(summary = "取消時段", description = "將指定時段狀態設為 CANCELLED")
    @DeleteMapping("/slots/{slotId}")
    public void cancelSlot(
            @Parameter(description = "時段 ID") @PathVariable UUID slotId) {
        log.info("取消時段，slotId: {}", slotId);
        timeSlotUseCase.cancelSlot(slotId);
    }
}
