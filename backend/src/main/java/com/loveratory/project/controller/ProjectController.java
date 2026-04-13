package com.loveratory.project.controller;

import com.loveratory.project.dto.request.ProjectCreateRequest;
import com.loveratory.project.dto.request.ProjectUpdateRequest;
import com.loveratory.project.dto.response.ProjectDetailResponse;
import com.loveratory.project.dto.response.ProjectSummaryResponse;
import com.loveratory.project.usecase.ProjectUseCase;
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
 * 專案管理 API。
 * 提供專案的建立、查詢、更新與封存功能。
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1")
@Tag(name = "專案管理", description = "專案建立、查詢、更新與封存")
public class ProjectController {

    private final ProjectUseCase projectUseCase;

    /**
     * 建立專案。
     *
     * @param labId   實驗室 ID
     * @param request 建立專案請求
     * @return 專案詳情回應
     */
    @Operation(summary = "建立專案", description = "在指定實驗室下建立新專案，狀態為 DRAFT")
    @PostMapping("/labs/{labId}/projects")
    public ProjectDetailResponse createProject(
            @Parameter(description = "實驗室 ID") @PathVariable UUID labId,
            @Valid @RequestBody ProjectCreateRequest request) {
        log.info("建立專案，labId: {}, name: {}", labId, request.getName());
        return projectUseCase.createProject(labId, request);
    }

    /**
     * 查詢實驗室的專案列表。
     *
     * @param labId 實驗室 ID
     * @return 專案摘要列表
     */
    @Operation(summary = "查詢專案列表", description = "查詢實驗室下的專案列表，LAB_ADMIN 可看全部，一般成員只看自己主持的")
    @GetMapping("/labs/{labId}/projects")
    public List<ProjectSummaryResponse> findProjects(
            @Parameter(description = "實驗室 ID") @PathVariable UUID labId) {
        log.info("查詢專案列表，labId: {}", labId);
        return projectUseCase.findProjects(labId);
    }

    /**
     * 查詢專案詳情。
     *
     * @param projectId 專案 ID
     * @return 專案詳情回應
     */
    @Operation(summary = "查詢專案詳情", description = "根據專案 ID 查詢完整資訊與主持人列表")
    @GetMapping("/projects/{projectId}")
    public ProjectDetailResponse findProjectDetail(
            @Parameter(description = "專案 ID") @PathVariable UUID projectId) {
        log.info("查詢專案詳情，projectId: {}", projectId);
        return projectUseCase.findProjectDetail(projectId);
    }

    /**
     * 更新專案。
     *
     * @param projectId 專案 ID
     * @param request   更新專案請求
     * @return 專案詳情回應
     */
    @Operation(summary = "更新專案", description = "更新專案名稱、描述與狀態")
    @PutMapping("/projects/{projectId}")
    public ProjectDetailResponse updateProject(
            @Parameter(description = "專案 ID") @PathVariable UUID projectId,
            @Valid @RequestBody ProjectUpdateRequest request) {
        log.info("更新專案，projectId: {}, name: {}", projectId, request.getName());
        return projectUseCase.updateProject(projectId, request);
    }

    /**
     * 封存專案。
     *
     * @param projectId 專案 ID
     */
    @Operation(summary = "封存專案", description = "將專案狀態設為 ARCHIVED")
    @DeleteMapping("/projects/{projectId}")
    public void archiveProject(
            @Parameter(description = "專案 ID") @PathVariable UUID projectId) {
        log.info("封存專案，projectId: {}", projectId);
        projectUseCase.archiveProject(projectId);
    }
}
