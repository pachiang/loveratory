package com.loveratory.registration.dto.request;

import com.loveratory.registration.entity.RegistrationStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * 更新報名狀態請求。
 */
@Getter
@Setter
@Schema(description = "更新報名狀態請求")
public class RegistrationStatusUpdateRequest {

    @NotNull(message = "報名狀態不可為空")
    @Schema(description = "報名狀態", example = "NO_SHOW")
    private RegistrationStatus status;
}
