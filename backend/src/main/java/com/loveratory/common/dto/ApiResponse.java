package com.loveratory.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

/**
 * 統一 API 回應包裹物件。
 */
@Getter
@Builder
@Schema(description = "統一 API 回應格式")
public class ApiResponse<T> {

    @Schema(description = "是否成功")
    private final boolean success;

    @Schema(description = "回應代碼")
    private final String code;

    @Schema(description = "回應訊息")
    private final String message;

    @Schema(description = "回應資料")
    private final T data;

    /**
     * 建立成功回應。
     */
    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .code("SUCCESS")
                .data(data)
                .build();
    }

    /**
     * 建立錯誤回應。
     */
    public static <T> ApiResponse<T> error(String code, String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .code(code)
                .message(message)
                .build();
    }
}
