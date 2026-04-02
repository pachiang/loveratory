package com.loveratory.lab.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 拒絕實驗室申請請求。
 */
@Getter
@Setter
@Schema(description = "拒絕實驗室申請請求")
public class LabRejectRequest {

    @NotBlank(message = "拒絕原因不可為空")
    @Schema(description = "拒絕原因", example = "申請資料不完整，請補充實驗室說明")
    private String reviewNote;
}
