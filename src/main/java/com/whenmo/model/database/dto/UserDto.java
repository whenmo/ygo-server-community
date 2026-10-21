package com.whenmo.model.database.dto;

import com.whenmo.model.database.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class UserDto {
    private Integer id;
    private String name;
    private Boolean isAdmin;

    public static UserDto fromUser(User user) {
        if (user == null)
            return null;

        return UserDto.builder().id(user.getId()).name(user.getName()).isAdmin(user.getIsAdmin()).build();
    }
}
