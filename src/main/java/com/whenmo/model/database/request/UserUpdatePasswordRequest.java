package com.whenmo.model.database.request;

import com.whenmo.model.database.domain.User;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdatePasswordRequest {

    @NotBlank(message = "新密碼不可為空")
    private String newPassword;

    @NotBlank(message = "舊密碼不可為空")
    private String oldPassword;

    public void updateUser(User user, String encodePasswordHash) {
        if (user == null)
            return;

        user.setPasswordHash(encodePasswordHash);
    }
}
