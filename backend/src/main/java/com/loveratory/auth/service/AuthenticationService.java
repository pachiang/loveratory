package com.loveratory.auth.service;

import com.loveratory.auth.dto.request.UserLoginRequest;
import com.loveratory.auth.dto.request.UserRegisterRequest;
import com.loveratory.auth.dto.response.UserLoginResponse;
import com.loveratory.auth.entity.UserEntity;
import com.loveratory.auth.entity.UserRole;
import com.loveratory.auth.manager.UserManager;
import com.loveratory.common.exception.BusinessException;
import com.loveratory.common.exception.ErrorCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 認證與使用者建立的共用業務邏輯。
 * 提供密碼驗證、使用者建立與 Token 發放等可跨模組共用的認證功能。
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class AuthenticationService {

    private final UserManager userManager;
    private final JwtTokenService jwtTokenService;
    private final PasswordEncoder passwordEncoder;

    /**
     * 驗證使用者帳號密碼。
     *
     * @param request 登入請求
     * @return 驗證通過的 UserEntity
     */
    public UserEntity authenticate(@NonNull UserLoginRequest request) {
        UserEntity userEntity = userManager.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        if (!passwordEncoder.matches(request.getPassword(), userEntity.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        return userEntity;
    }

    /**
     * 為使用者發放 Access Token 與 Refresh Token。
     *
     * @param userEntity 使用者 Entity
     * @return 登入回應（含 Token 與使用者資料）
     */
    public UserLoginResponse issueTokens(@NonNull UserEntity userEntity) {
        String accessToken = jwtTokenService.generateAccessToken(
                userEntity.getId(), userEntity.getEmail(), userEntity.getRole());
        String refreshToken = jwtTokenService.generateRefreshToken(userEntity.getId());
        return UserLoginResponse.fromEntity(userEntity, accessToken, refreshToken);
    }

    /**
     * 建立使用者。
     * 檢查 Email 是否已存在，建立並回傳使用者。
     *
     * @param request 註冊請求
     * @param role    使用者角色
     * @return 建立的 UserEntity
     */
    public UserEntity createUser(@NonNull UserRegisterRequest request, @NonNull UserRole role) {
        if (userManager.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        UserEntity userEntity = new UserEntity();
        userEntity.setEmail(request.getEmail());
        userEntity.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        userEntity.setName(request.getName());
        userEntity.setRole(role);
        return userManager.save(userEntity);
    }
}
