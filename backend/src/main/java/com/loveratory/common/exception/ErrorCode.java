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
    INVITATION_ALREADY_ACCEPTED("INVITATION_004", "邀請已被接受", CONFLICT);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
