package com.whenmo.model.database.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginRequest {

    @NotBlank(message = "用戶名稱不可為空")
    private String name;

    @NotBlank(message = "密碼不可為空")
    private String password;
}