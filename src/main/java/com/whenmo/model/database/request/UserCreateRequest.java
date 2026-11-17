package com.whenmo.model.database.request;

import com.whenmo.model.database.domain.User;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateRequest {

    @NotBlank(message = "用戶名稱不可為空")
    private String name;

    @NotBlank(message = "密碼不可為空")
    private String password;

    public User toUser(String trimName, String encodePasswordHash) {
        return User.builder().name(trimName).passwordHash(encodePasswordHash).isAdmin(false).build();
    }
}