package com.loveratory.lab.dto.response;

import com.loveratory.auth.dto.response.UserLoginResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Getter
@Builder
@Schema(description = "Invitation access response")
public class InvitationAccessResponse {

    @Schema(description = "Authenticated user tokens and profile")
    private final UserLoginResponse auth;

    @Schema(description = "Accepted invitation details")
    private final LabInvitationResponse invitation;

    public static InvitationAccessResponse of(@NonNull UserLoginResponse auth,
                                              @NonNull LabInvitationResponse invitation) {
        return InvitationAccessResponse.builder()
                .auth(auth)
                .invitation(invitation)
                .build();
    }
}
