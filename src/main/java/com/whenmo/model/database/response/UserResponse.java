package com.whenmo.model.database.response;

import com.whenmo.model.database.domain.User;
import com.whenmo.model.database.dto.DiyerDto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Integer id;
    private String name;
    private Boolean isAdmin;
    private DiyerDto diyer;

    public static UserResponse fromModel(User model) {
        if (model == null)
            return null;

        return UserResponse.builder().id(model.getId()).name(model.getName()).isAdmin(model.getIsAdmin())
                .diyer(DiyerDto.fromModel(model.getDiyer())).build();
    }
}