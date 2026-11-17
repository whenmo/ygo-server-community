package com.whenmo.model.database.dto;

import org.springframework.core.io.Resource;
import lombok.*;

@Data
@AllArgsConstructor
public class YdkDto {
    private String name;
    private Resource resource;
}
