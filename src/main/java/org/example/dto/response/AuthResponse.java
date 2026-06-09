package org.example.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
    private UserInfoDTO user;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class UserInfoDTO {
        private Long id;
        private String username;
        private String email;
        private String fullName;
        private String phoneNumber;
        private String role;
        private Boolean isActive;
    }
}
