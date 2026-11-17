package com.whenmo.model.database.request;

import com.whenmo.model.database.domain.User;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateInfoRequest {

    @NotBlank(message = "用戶名稱不可為空")
    private String name;

    public void updateModel(User model, String trimName) {
        if (model == null)
            return;

        model.setName(trimName);
    }
}