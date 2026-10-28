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
    private String picCdn;

    public void updateModel(CdbConfig model) {
        if (model == null)
            return;

        if (this.name != null)
            model.setName(this.name);
        if (this.filePath != null)
            model.setFilePath(this.filePath);
        if (this.picCdn != null)
            model.setPicCdn(this.picCdn);
    }
}