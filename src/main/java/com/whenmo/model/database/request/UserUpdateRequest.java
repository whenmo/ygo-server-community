package com.whenmo.model.database.request;

import com.whenmo.model.database.domain.User;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {

    @Builder.Default
    private String name = null;

    @Builder.Default
    private String newPassword = null;

    @Builder.Default
    private Boolean isAdmin = false;

    public void updateUser(User user, String trimName, String encodePasswordHash) {
        if (user == null)
            return;
        if (trimName != null)
            user.setName(trimName);
        if (encodePasswordHash != null)
            user.setPasswordHash(encodePasswordHash);
        user.setIsAdmin(Boolean.TRUE.equals(isAdmin));
    }
}
