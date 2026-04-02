package com.loveratory.registration.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * 受試者報名請求。
 */
@Getter
@Setter
@Schema(description = "受試者報名請求")
public class ParticipantRegistrationRequest {

    @NotNull(message = "時段 ID 不可為空")
    @Schema(description = "時段 ID")
    private UUID slotId;

    @NotBlank(message = "Email 不可為空")
    @Email(message = "Email 格式不正確")
    @Schema(description = "受試者 Email", example = "participant@example.com")
    private String email;

    @Schema(description = "受試者姓名", example = "王小明")
    private String name;

    @Schema(description = "受試者手機", example = "0912345678")
    private String phone;

    @Schema(description = "受試者學號", example = "B10901001")
    private String studentId;

    @Schema(description = "受試者年齡", example = "22")
    private Integer age;

    @Schema(description = "受試者性別", example = "男")
    private String gender;

    @Schema(description = "受試者慣用手", example = "右")
    private String dominantHand;

    @Schema(description = "備註")
    private String notes;
}
