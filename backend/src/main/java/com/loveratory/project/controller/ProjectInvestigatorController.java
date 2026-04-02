package com.loveratory.project.controller;

import com.loveratory.project.dto.request.InvestigatorAddRequest;
import com.loveratory.project.dto.response.InvestigatorResponse;
import com.loveratory.project.usecase.ProjectInvestigatorUseCase;
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

import java.util.List;
import java.util.UUID;

/**
 * 專案主持人管理 API。
 * 提供主持人的查詢、新增與移除功能。
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/projects/{projectId}/investigators")
@Tag(name = "專案主持人管理", description = "專案主持人查詢、新增與移除")
public class ProjectInvestigatorController {

    private final ProjectInvestigatorUseCase projectInvestigatorUseCase;

    /**
     * 查詢專案的主持人列表。
     *
     * @param projectId 專案 ID
     * @return 主持人回應列表
     */
    @Operation(summary = "查詢主持人列表", description = "查詢專案的所有啟用中主持人")
    @GetMapping
    public List<InvestigatorResponse> findInvestigators(
            @Parameter(description = "專案 ID") @PathVariable UUID projectId) {
        log.info("查詢主持人列表，projectId: {}", projectId);
        return projectInvestigatorUseCase.findInvestigators(projectId);
    }

    /**
     * 新增專案主持人。
     *
     * @param projectId 專案 ID
     * @param request   新增主持人請求
     * @return 主持人回應
     */
    @Operation(summary = "新增主持人", description = "將實驗室成員新增為專案主持人")
    @PostMapping
    public InvestigatorResponse addInvestigator(
            @Parameter(description = "專案 ID") @PathVariable UUID projectId,
            @Valid @RequestBody InvestigatorAddRequest request) {
        log.info("新增主持人，projectId: {}, userId: {}", projectId, request.getUserId());
        return projectInvestigatorUseCase.addInvestigator(projectId, request);
    }

    /**
     * 移除專案主持人。
     *
     * @param projectId 專案 ID
     * @param userId    要移除的使用者 ID
     */
    @Operation(summary = "移除主持人", description = "從專案中移除主持人，專案至少需保留一位")
    @DeleteMapping("/{userId}")
    public void removeInvestigator(
            @Parameter(description = "專案 ID") @PathVariable UUID projectId,
            @Parameter(description = "要移除的使用者 ID") @PathVariable UUID userId) {
        log.info("移除主持人，projectId: {}, userId: {}", projectId, userId);
        projectInvestigatorUseCase.removeInvestigator(projectId, userId);
    }
}
