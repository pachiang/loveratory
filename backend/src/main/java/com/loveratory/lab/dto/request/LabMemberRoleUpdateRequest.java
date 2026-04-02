package com.loveratory.lab.dto.request;

import com.loveratory.lab.entity.LabMemberRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * 更新實驗室成員角色請求。
 */
@Getter
@Setter
@Schema(description = "更新實驗室成員角色請求")
public class LabMemberRoleUpdateRequest {

    @NotNull(message = "角色不可為空")
    @Schema(description = "成員角色", example = "LAB_MEMBER")
    private LabMemberRole role;
}
