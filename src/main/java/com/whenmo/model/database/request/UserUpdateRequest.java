package com.whenmo.model.database.request;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.whenmo.model.database.domain.User;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {

    private String name;
    private String password;
    private Boolean isAdmin;

    public void setName(String name) {
        this.name = (name != null && name.isBlank()) ? null : name;
    }

    public void setPassword(String password) {
        this.password = (password != null && password.isBlank()) ? null : password;
    }

    public void updateModel(User model, PasswordEncoder passwordEncoder) {
        if (model == null)
            return;

        if (this.name != null)
            model.setName(this.name);
        if (this.password != null && passwordEncoder != null)
            model.setPasswordHash(passwordEncoder.encode(this.password));
        if (this.isAdmin != null)
            model.setIsAdmin(this.isAdmin);
    }
}