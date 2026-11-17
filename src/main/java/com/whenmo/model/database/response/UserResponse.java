package com.whenmo.model.database.response;

import com.whenmo.model.database.domain.User;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Integer id;
    private String name;
    private Boolean isAdmin;

    public static UserResponse fromUser(User model) {
        if (model == null)
            return null;

        return new UserResponse(model.getId(), model.getName(), model.getIsAdmin());
    }
}