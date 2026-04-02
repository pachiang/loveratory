package com.loveratory.lab.dto.response;

import com.loveratory.auth.entity.UserEntity;
import com.loveratory.lab.entity.LabMemberEntity;
import com.loveratory.lab.entity.LabMemberRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * 實驗室成員回應。
 * 包含成員的使用者資訊與角色。
 */
@Getter
@Builder
@Schema(description = "實驗室成員回應")
public class LabMemberResponse {

    @Schema(description = "使用者 ID")
    private final UUID userId;

    @Schema(description = "使用者姓名")
    private final String name;

    @Schema(description = "使用者 Email")
    private final String email;

    @Schema(description = "成員角色")
    private final LabMemberRole role;

    @Schema(description = "加入時間")
    private final ZonedDateTime joinedAt;

    /**
     * 從 LabMemberEntity 與 UserEntity 建立成員回應。
     *
     * @param memberEntity 成員 Entity
     * @param userEntity   使用者 Entity
     * @return 實驗室成員回應
     */
    public static LabMemberResponse of(@NonNull LabMemberEntity memberEntity,
                                       @NonNull UserEntity userEntity) {
        return LabMemberResponse.builder()
                .userId(userEntity.getId())
                .name(userEntity.getName())
                .email(userEntity.getEmail())
                .role(memberEntity.getRole())
                .joinedAt(memberEntity.getJoinedAt())
                .build();
    }
}
