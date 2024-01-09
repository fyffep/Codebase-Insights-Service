package com.insightservice.springboot.model.codebase;

import java.io.Serializable;

public class CodebaseId implements Serializable {
    private String githubUrl;
    private String branchName;

    public void setGithubUrl(String githubUrl) {
        this.githubUrl = githubUrl;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getGithubUrl() {
        return this.githubUrl;
    }

    public String getBranchNAme() {
        return this.branchName;
    }

}
