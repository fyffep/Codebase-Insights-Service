package com.insightservice.springboot.model.fileComparisionBranch;

import com.insightservice.springboot.payload.SettingsPayload;

public class SettingPayloadsWFilename {
    public SettingsPayload settingsPayload;
    public String filename;

    SettingPayloadsWFilename(SettingsPayload settingsPayload, String filename) {
        this.filename = filename;
        this.settingsPayload = settingsPayload;
    }

    public SettingsPayload gSettingsPayload() {
        return this.settingsPayload;
    }

    public String gFilename() {
        return this.filename;
    }

}
