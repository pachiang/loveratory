package com.loveratory.lab.controller;

import com.loveratory.lab.dto.request.LabMemberRoleUpdateRequest;
import com.loveratory.lab.dto.response.LabMemberResponse;
import com.loveratory.lab.usecase.LabMemberUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * 實驗室成員管理 API。
 * 提供成員查詢、角色變更與移除功能。
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/labs/{labId}/members")
@Tag(name = "實驗室成員管理", description = "成員查詢、角色變更與移除")
public class LabMemberController {

    private final LabMemberUseCase labMemberUseCase;

    /**
     * 查詢實驗室成員列表。
     *
     * @param labId 實驗室 ID
     * @return 成員列表
     */
    @Operation(summary = "查詢成員列表", description = "列出實驗室所有啟用中成員")
    @GetMapping
    public List<LabMemberResponse> findMembers(
            @Parameter(description = "實驗室 ID") @PathVariable UUID labId) {
        log.info("查詢實驗室成員列表，labId: {}", labId);
        return labMemberUseCase.findMembers(labId);
    }

    /**
     * 更新成員角色。
     *
     * @param labId   實驗室 ID
     * @param userId  目標成員使用者 ID
     * @param request 角色更新請求
     */
    @Operation(summary = "更新成員角色", description = "變更實驗室成員的角色（僅限管理員）")
    @PutMapping("/{userId}/role")
    public void updateMemberRole(
            @Parameter(description = "實驗室 ID") @PathVariable UUID labId,
            @Parameter(description = "目標成員使用者 ID") @PathVariable UUID userId,
            @Valid @RequestBody LabMemberRoleUpdateRequest request) {
        log.info("更新成員角色，labId: {}, userId: {}, role: {}", labId, userId, request.getRole());
        labMemberUseCase.updateMemberRole(labId, userId, request);
    }

    /**
     * 移除成員。
     *
     * @param labId  實驗室 ID
     * @param userId 目標成員使用者 ID
     */
    @Operation(summary = "移除成員", description = "將成員從實驗室移除（僅限管理員）")
    @DeleteMapping("/{userId}")
    public void removeMember(
            @Parameter(description = "實驗室 ID") @PathVariable UUID labId,
            @Parameter(description = "目標成員使用者 ID") @PathVariable UUID userId) {
        log.info("移除實驗室成員，labId: {}, userId: {}", labId, userId);
        labMemberUseCase.removeMember(labId, userId);
    }
}
