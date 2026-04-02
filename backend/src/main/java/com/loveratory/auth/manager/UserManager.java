package com.loveratory.auth.manager;

import com.loveratory.auth.entity.UserEntity;
import com.loveratory.auth.repository.UserRepository;
import com.loveratory.common.exception.BusinessException;
import com.loveratory.common.exception.ErrorCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * 使用者資料存取管理器。
 * 封裝 UserEntity 的所有 CRUD 操作。
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class UserManager {

    private final UserRepository userRepository;

    /**
     * 根據 ID 查詢使用者，找不到時拋出 BusinessException。
     *
     * @param userId 使用者 ID
     * @return 使用者 Entity
     */
    public UserEntity findByIdOrThrow(@NonNull UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    /**
     * 根據 Email 查詢使用者。
     *
     * @param email 使用者 Email
     * @return 使用者 Optional
     */
    public Optional<UserEntity> findByEmail(@NonNull String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * 檢查 Email 是否已存在。
     *
     * @param email 使用者 Email
     * @return 是否已存在
     */
    public boolean existsByEmail(@NonNull String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * 儲存使用者。
     *
     * @param userEntity 使用者 Entity
     * @return 儲存後的使用者 Entity
     */
    public UserEntity save(@NonNull UserEntity userEntity) {
        return userRepository.save(userEntity);
    }
}
