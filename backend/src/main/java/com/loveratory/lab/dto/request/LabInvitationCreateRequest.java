package com.loveratory.lab.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 建立實驗室邀請請求。
 */
@Getter
@Setter
@Schema(description = "建立實驗室邀請請求")
public class LabInvitationCreateRequest {

    @NotBlank(message = "Email 不可為空")
    @Email(message = "Email 格式不正確")
    @Schema(description = "被邀請者 Email", example = "member@example.com")
    private String email;
}
