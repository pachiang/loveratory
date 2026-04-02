package com.loveratory.lab.controller;

import com.loveratory.lab.dto.request.LabInvitationCreateRequest;
import com.loveratory.lab.dto.response.LabInvitationResponse;
import com.loveratory.lab.usecase.LabInvitationUseCase;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * 實驗室邀請管理 API。
 * 提供邀請的建立、查詢、撤銷與接受功能。
 * 涵蓋 /api/labs/{labId}/invitations 與 /api/invitations/{token} 兩組路徑。
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@Tag(name = "實驗室邀請管理", description = "邀請建立、查詢、撤銷與接受")
public class LabInvitationController {

    private final LabInvitationUseCase labInvitationUseCase;

    /**
     * 查詢實驗室的邀請列表。
     *
     * @param labId 實驗室 ID
     * @return 邀請列表
     */
    @Operation(summary = "查詢邀請列表", description = "列出實驗室所有邀請紀錄（僅限管理員）")
    @GetMapping("/api/labs/{labId}/invitations")
    public List<LabInvitationResponse> findInvitations(
            @Parameter(description = "實驗室 ID") @PathVariable UUID labId) {
        log.info("查詢實驗室邀請列表，labId: {}", labId);
        return labInvitationUseCase.findInvitations(labId);
    }

    /**
     * 建立實驗室邀請。
     *
     * @param labId   實驗室 ID
     * @param request 建立邀請請求
     * @return 邀請回應
     */
    @Operation(summary = "建立邀請", description = "輸入 Email 發送實驗室邀請（僅限管理員）")
    @PostMapping("/api/labs/{labId}/invitations")
    public LabInvitationResponse createInvitation(
            @Parameter(description = "實驗室 ID") @PathVariable UUID labId,
            @Valid @RequestBody LabInvitationCreateRequest request) {
        log.info("建立實驗室邀請，labId: {}, email: {}", labId, request.getEmail());
        return labInvitationUseCase.createInvitation(labId, request);
    }

    /**
     * 撤銷實驗室邀請。
     *
     * @param labId        實驗室 ID
     * @param invitationId 邀請 ID
     */
    @Operation(summary = "撤銷邀請", description = "將邀請狀態設為已過期（僅限管理員）")
    @DeleteMapping("/api/labs/{labId}/invitations/{invitationId}")
    public void revokeInvitation(
            @Parameter(description = "實驗室 ID") @PathVariable UUID labId,
            @Parameter(description = "邀請 ID") @PathVariable UUID invitationId) {
        log.info("撤銷實驗室邀請，labId: {}, invitationId: {}", labId, invitationId);
        labInvitationUseCase.revokeInvitation(labId, invitationId);
    }

    /**
     * 查看邀請資訊。
     * 此為公開 API，不需登入即可查看。
     *
     * @param token 邀請 Token
     * @return 邀請回應
     */
    @Operation(summary = "查看邀請資訊", description = "根據邀請 Token 查看邀請詳情（公開）")
    @GetMapping("/api/invitations/{token}")
    public LabInvitationResponse findInvitationByToken(
            @Parameter(description = "邀請 Token") @PathVariable String token) {
        log.info("查看邀請資訊，token: {}", token);
        return labInvitationUseCase.findInvitationByToken(token);
    }

    /**
     * 接受邀請加入實驗室。
     * 需要使用者已登入。
     *
     * @param token 邀請 Token
     */
    @Operation(summary = "接受邀請", description = "接受邀請加入實驗室（需登入）")
    @PostMapping("/api/invitations/{token}/accept")
    public void acceptInvitation(
            @Parameter(description = "邀請 Token") @PathVariable String token) {
        log.info("接受實驗室邀請，token: {}", token);
        labInvitationUseCase.acceptInvitation(token);
    }
}
