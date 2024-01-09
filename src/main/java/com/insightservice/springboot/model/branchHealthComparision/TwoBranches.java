package com.insightservice.springboot.model.branchHealthComparision;

import org.springframework.data.util.Pair;

/* This file will serve as a RequestBody for HealthComparisionBranch controller */
public class TwoBranches {
    public String parentBranch;
    public String childBranch;

    TwoBranches(String parentBranch, String childBranch) {
        this.parentBranch = parentBranch;
        this.childBranch = childBranch;
    }

    public Pair<String, String> getBranches() {
        return Pair.of(this.parentBranch, this.childBranch);
    }

}
