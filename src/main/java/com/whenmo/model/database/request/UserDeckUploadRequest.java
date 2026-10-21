package com.whenmo.model.database.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDeckUploadRequest {

    private String name;
    private Boolean isPublic;
    private String packName;
}