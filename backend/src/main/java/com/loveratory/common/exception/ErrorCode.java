package com.loveratory.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

/**
 * 系統錯誤代碼。
 * 代碼格式：{領域}_{序號}，例如 AUTH_001。
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // ===== 通用錯誤 =====
    SYSTEM_ERROR("SYSTEM_001", "系統錯誤", INTERNAL_SERVER_ERROR),
    INVALID_PARAMETER("SYSTEM_002", "請求參數錯誤", BAD_REQUEST),
    RESOURCE_NOT_FOUND("SYSTEM_003", "資源不存在", NOT_FOUND),
    ACCESS_DENIED("SYSTEM_004", "存取被拒", FORBIDDEN),

    // ===== 認證相關 =====
    EMAIL_ALREADY_EXISTS("AUTH_001", "Email 已被註冊", CONFLICT),
    INVALID_CREDENTIALS("AUTH_002", "帳號或密碼錯誤", UNAUTHORIZED),
    INVALID_TOKEN("AUTH_003", "無效的 Token", UNAUTHORIZED),
    TOKEN_EXPIRED("AUTH_004", "Token 已過期", UNAUTHORIZED),
    USER_NOT_FOUND("AUTH_005", "使用者不存在", NOT_FOUND),

    // ===== 實驗室相關 =====
    LAB_NOT_FOUND("LAB_001", "實驗室不存在", NOT_FOUND),
    LAB_ALREADY_APPROVED("LAB_002", "實驗室已通過審核", CONFLICT),
    INVALID_LAB_STATUS("LAB_003", "實驗室狀態不允許此操作", BAD_REQUEST),
    LAB_MEMBER_NOT_FOUND("LAB_004", "實驗室成員不存在", NOT_FOUND),
    LAB_MEMBER_ALREADY_EXISTS("LAB_005", "該使用者已是實驗室成員", CONFLICT),
    NOT_LAB_ADMIN("LAB_006", "需要實驗室管理員權限", FORBIDDEN),

    // ===== 邀請相關 =====
    INVITATION_NOT_FOUND("INVITATION_001", "邀請不存在", NOT_FOUND),
    INVITATION_EXPIRED("INVITATION_002", "邀請已過期", BAD_REQUEST),
    INVITATION_ALREADY_EXISTS("INVITATION_003", "已有未過期的邀請", CONFLICT),
    INVITATION_ALREADY_ACCEPTED("INVITATION_004", "邀請已被接受", CONFLICT),

    // ===== 專案相關 =====
    PROJECT_NOT_FOUND("PROJECT_001", "專案不存在", NOT_FOUND),
    INVALID_PROJECT_STATUS("PROJECT_002", "專案狀態不允許此操作", BAD_REQUEST),
    NOT_PROJECT_INVESTIGATOR("PROJECT_003", "需要專案主持人權限", FORBIDDEN),
    LAST_INVESTIGATOR_CANNOT_REMOVE("PROJECT_004", "專案至少需要一位主持人", BAD_REQUEST),
    INVESTIGATOR_ALREADY_EXISTS("PROJECT_005", "該使用者已是專案主持人", CONFLICT),
    NOT_LAB_MEMBER("PROJECT_006", "該使用者不是實驗室成員", BAD_REQUEST),

    // ===== 實驗相關 =====
    EXPERIMENT_NOT_FOUND("EXPERIMENT_001", "實驗不存在", NOT_FOUND),
    INVALID_EXPERIMENT_STATUS("EXPERIMENT_002", "實驗狀態不允許此操作", BAD_REQUEST),
    EXPERIMENT_SLUG_ALREADY_EXISTS("EXPERIMENT_003", "實驗連結代碼已存在", CONFLICT),
    CANCEL_POLICY_LOCKED("EXPERIMENT_004", "實驗開放後不可更改取消政策", BAD_REQUEST),

    // ===== 時段相關 =====
    SLOT_NOT_FOUND("SLOT_001", "時段不存在", NOT_FOUND),
    SLOT_FULL("SLOT_002", "時段已額滿", BAD_REQUEST),
    INVALID_SLOT_STATUS("SLOT_003", "時段狀態不允許此操作", BAD_REQUEST),

    // ===== 報名相關 =====
    REGISTRATION_NOT_FOUND("REGISTRATION_001", "報名不存在", NOT_FOUND),
    DUPLICATE_REGISTRATION("REGISTRATION_002", "該 Email 已報名此實驗", CONFLICT),
    REGISTRATION_CANCEL_NOT_ALLOWED("REGISTRATION_003", "此實驗不允許取消報名", BAD_REQUEST),
    REGISTRATION_ALREADY_CANCELLED("REGISTRATION_004", "報名已取消", CONFLICT);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
