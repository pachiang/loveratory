package com.loveratory.auth.repository;

import com.loveratory.auth.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

/**
 * 使用者 Repository。
 * 提供 UserEntity 的資料存取介面。
 */
public interface UserRepository extends JpaRepository<UserEntity, UUID>,
        JpaSpecificationExecutor<UserEntity> {

    /**
     * 根據 Email 查詢使用者。
     *
     * @param email 使用者 Email
     * @return 使用者 Optional
     */
    Optional<UserEntity> findByEmail(String email);

    /**
     * 檢查 Email 是否已存在。
     *
     * @param email 使用者 Email
     * @return 是否已存在
     */
    boolean existsByEmail(String email);
}
