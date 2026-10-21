package com.whenmo.model.database.request;

import com.whenmo.model.database.domain.CdbConfig;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CdbConfigUpdateRequest {

    private String name;
    private String filePath;

    public void setName(String name) {
        this.name = (name != null && name.isBlank()) ? null : name;
    }

    public void setFilePath(String filePath) {
        this.filePath = (filePath != null && filePath.isBlank()) ? null : filePath;
    }

    public void updateModel(CdbConfig model) {
        if (model == null)
            return;

        if (this.name != null)
            model.setName(this.name);
        if (this.filePath != null)
            model.setFilePath(this.filePath);
    }
}