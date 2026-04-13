package com.loveratory.registration.controller;

import com.loveratory.registration.dto.request.ParticipantRegistrationRequest;
import com.loveratory.registration.dto.response.PublicExperimentResponse;
import com.loveratory.registration.dto.response.PublicRegistrationResponse;
import com.loveratory.registration.usecase.PublicRegistrationUseCase;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 受試者報名 API（公開）。
 * 提供受試者的實驗查詢、報名、查看報名與取消報名功能，不需要登入。
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/public")
@Tag(name = "受試者報名（公開）", description = "受試者實驗查詢、報名與取消（免登入）")
public class PublicExperimentController {

    private final PublicRegistrationUseCase publicRegistrationUseCase;

    /**
     * 查詢公開實驗資訊。
     *
     * @param slug 實驗連結代碼
     * @return 公開實驗回應
     */
    @Operation(summary = "查詢實驗資訊", description = "根據 slug 取得實驗說明、表單設定與可報名時段")
    @GetMapping("/experiments/{slug}")
    public PublicExperimentResponse findExperimentBySlug(
            @Parameter(description = "實驗連結代碼") @PathVariable String slug) {
        log.info("查詢公開實驗資訊，slug: {}", slug);
        return publicRegistrationUseCase.findExperimentBySlug(slug);
    }

    /**
     * 受試者報名。
     *
     * @param slug    實驗連結代碼
     * @param request 報名請求
     * @return 公開報名回應
     */
    @Operation(summary = "報名", description = "受試者報名指定實驗時段，回傳 cancel_token")
    @PostMapping("/experiments/{slug}/register")
    public PublicRegistrationResponse register(
            @Parameter(description = "實驗連結代碼") @PathVariable String slug,
            @Valid @RequestBody ParticipantRegistrationRequest request) {
        log.info("受試者報名，slug: {}, email: {}, slotId: {}", slug, request.getEmail(), request.getSlotId());
        return publicRegistrationUseCase.register(slug, request);
    }

    /**
     * 查詢報名狀態。
     *
     * @param cancelToken 取消 token
     * @return 公開報名回應
     */
    @Operation(summary = "查詢報名狀態", description = "根據 cancel_token 查看報名資訊")
    @GetMapping("/registrations/{cancelToken}")
    public PublicRegistrationResponse findRegistrationByToken(
            @Parameter(description = "取消 token") @PathVariable String cancelToken) {
        log.info("查詢報名狀態，cancelToken: {}", cancelToken);
        return publicRegistrationUseCase.findRegistrationByToken(cancelToken);
    }

    /**
     * 取消報名。
     *
     * @param cancelToken 取消 token
     * @return 公開報名回應
     */
    @Operation(summary = "取消報名", description = "受試者自行取消報名（需實驗允許取消）")
    @DeleteMapping("/registrations/{cancelToken}")
    public PublicRegistrationResponse cancelRegistration(
            @Parameter(description = "取消 token") @PathVariable String cancelToken) {
        log.info("取消報名，cancelToken: {}", cancelToken);
        return publicRegistrationUseCase.cancelRegistration(cancelToken);
    }
}
