package com.insightservice.springboot.model.branchHealthComparision;

import com.insightservice.springboot.payload.SettingsPayload;

public class SettingPayloadWBranches {
    public SettingsPayload settingsPayload;
    public TwoBranches twoBranches;

    SettingPayloadWBranches(SettingsPayload settingsPayload, TwoBranches twoBranches) {
        this.settingsPayload = settingsPayload;
        this.twoBranches = twoBranches;
    }

    public SettingsPayload gSettingsPayload() {
        return this.settingsPayload;
    }

    public TwoBranches gBranches() {
        return this.twoBranches;
    }
}
