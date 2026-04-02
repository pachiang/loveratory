package com.loveratory.project.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * 新增專案主持人請求。
 */
@Getter
@Setter
@Schema(description = "新增專案主持人請求")
public class InvestigatorAddRequest {

    @NotNull(message = "使用者 ID 不可為空")
    @Schema(description = "要新增為主持人的使用者 ID")
    private UUID userId;
}
