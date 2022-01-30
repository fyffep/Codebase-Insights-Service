package com.insightservice.springboot.payload;

/**
 * A bean that contains a URL submitted by a user.
 * Example: user submits the URL to their GitHub repos with this object.
 */
public class UrlResquest
{
    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
