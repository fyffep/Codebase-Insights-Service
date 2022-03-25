package com.insightservice.springboot.payload;

import javax.validation.constraints.NotBlank;

/**
 * A bean that contains a URL submitted by a user.
 * Example: user submits the URL to their GitHub repos with this object.
 */
public class SettingsPayload
{
    @NotBlank
    private String githubUrl;
    @NotBlank
    private String branchName = "";
    @NotBlank
    private String githubOAuthToken;
    @NotBlank
    private String ciToolChosen;

    //Optional args
    private String ciUsername = null; //Jenkins only
    private String jobUrl = null; //Jenkins only
    private String apiKey = null; //Jenkins only


    //region getters/setters
    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getGithubOAuthToken() {
        return githubOAuthToken;
    }

    public void setGithubOAuthToken(String githubOAuthToken) {
        this.githubOAuthToken = githubOAuthToken;
    }

    public String getCiToolChosen() {
        return ciToolChosen;
    }

    public void setCiToolChosen(String ciToolChosen) {
        this.ciToolChosen = ciToolChosen;
    }

    public String getCiUsername() {
        return ciUsername;
    }

    public void setCiUsername(String ciUsername) {
        this.ciUsername = ciUsername;
    }

    public String getJobUrl() {
        return jobUrl;
    }

    public void setJobUrl(String jobUrl) {
        this.jobUrl = jobUrl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getGithubUrl() {
        return githubUrl;
    }

    public void setGithubUrl(String githubUrl) {
        this.githubUrl = githubUrl;
    }
    //endregion
}
