package com.loveratory.common.exception;

import lombok.Getter;
import lombok.NonNull;

/**
 * 業務例外。
 * 所有可預期的業務錯誤都應使用此例外拋出。
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    /**
     * 以 ErrorCode 的預設訊息建立例外。
     */
    public BusinessException(@NonNull ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    /**
     * 以自訂訊息建立例外。
     * 適用於需要附帶動態資訊的場景（如含具體的 ID、數值等）。
     */
    public BusinessException(@NonNull ErrorCode errorCode, @NonNull String detailMessage) {
        super(detailMessage);
        this.errorCode = errorCode;
    }

    /**
     * 以 ErrorCode 建立例外的靜態工廠方法。
     */
    public static BusinessException of(@NonNull ErrorCode errorCode) {
        return new BusinessException(errorCode);
    }

    /**
     * 以自訂訊息建立例外的靜態工廠方法。
     */
    public static BusinessException of(@NonNull ErrorCode errorCode,
                                        @NonNull String detailMessage) {
        return new BusinessException(errorCode, detailMessage);
    }
}
