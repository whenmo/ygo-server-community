package com.whenmo.model.database.request;

import org.springframework.security.crypto.password.PasswordEncoder;

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

    @Builder.Default
    private Boolean isAdmin = false;

    public User toModel(PasswordEncoder passwordEncoder) {
        return User.builder().name(this.name).passwordHash(passwordEncoder.encode(this.password))
                .isAdmin(Boolean.TRUE.equals(this.isAdmin)).build();
    }
}