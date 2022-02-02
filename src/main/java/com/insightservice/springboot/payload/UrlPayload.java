package com.insightservice.springboot.payload;

/**
 * A bean that contains a URL submitted by a user.
 * Example: user submits the URL to their GitHub repos with this object.
 */
public class UrlPayload
{
    private String githubUrl;

    public String getGithubUrl() {
        return githubUrl;
    }

    public void setGithubUrl(String githubUrl) {
        this.githubUrl = githubUrl;
    }
}
