package com.loveratory.registration.controller;

import com.loveratory.registration.dto.request.RegistrationStatusUpdateRequest;
import com.loveratory.registration.dto.response.RegistrationResponse;
import com.loveratory.registration.usecase.RegistrationUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * 報名管理 API（後台）。
 * 提供報名記錄的查詢與狀態管理功能。
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1")
@Tag(name = "報名管理（後台）", description = "報名記錄查詢與狀態管理")
public class RegistrationController {

    private final RegistrationUseCase registrationUseCase;

    /**
     * 查詢實驗的所有報名記錄。
     *
     * @param experimentId 實驗 ID
     * @return 報名回應列表
     */
    @Operation(summary = "查詢報名列表", description = "列出指定實驗的所有報名記錄")
    @GetMapping("/experiments/{experimentId}/registrations")
    public List<RegistrationResponse> findRegistrations(
            @Parameter(description = "實驗 ID") @PathVariable UUID experimentId) {
        log.info("查詢報名列表，experimentId: {}", experimentId);
        return registrationUseCase.findRegistrations(experimentId);
    }

    /**
     * 更新報名狀態。
     *
     * @param registrationId 報名 ID
     * @param request        更新狀態請求
     * @return 報名回應
     */
    @Operation(summary = "更新報名狀態", description = "更新報名狀態（如標記 NO_SHOW）")
    @PutMapping("/registrations/{registrationId}/status")
    public RegistrationResponse updateRegistrationStatus(
            @Parameter(description = "報名 ID") @PathVariable UUID registrationId,
            @Valid @RequestBody RegistrationStatusUpdateRequest request) {
        log.info("更新報名狀態，registrationId: {}, status: {}", registrationId, request.getStatus());
        return registrationUseCase.updateRegistrationStatus(registrationId, request);
    }
}
