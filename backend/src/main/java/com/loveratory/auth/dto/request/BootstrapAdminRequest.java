package com.loveratory.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Bootstrap system admin request")
public class BootstrapAdminRequest {

    @NotBlank(message = "Name must not be blank")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    @Schema(description = "Admin name", example = "System Admin")
    private String name;

    @NotBlank(message = "Email must not be blank")
    @Email(message = "Email format is invalid")
    @Schema(description = "Admin email", example = "admin@example.com")
    private String email;

    @NotBlank(message = "Password must not be blank")
    @Size(min = 8, max = 100, message = "Password length must be between 8 and 100 characters")
    @Schema(description = "Admin password", example = "ChangeMe123!")
    private String password;

    @NotBlank(message = "Bootstrap secret must not be blank")
    @Schema(description = "Bootstrap secret", example = "bootstrap-secret")
    private String bootstrapSecret;
}
