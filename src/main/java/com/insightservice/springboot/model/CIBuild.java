package com.insightservice.springboot.model;

public class CIBuild
{
    private long duration;
    private int number;
    private String result; //"SUCCESS" or "FAILURE"
    private String url; //e.g. "https://<JENKINS HOST>/job/<JOB NAME>/<BUILD NUMBER>/"
    private String commitHash;
    private String logUrl;

    public CIBuild() {
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getCommitHash() {
        return commitHash;
    }

    public void setCommitHash(String commitHash) {
        this.commitHash = commitHash;
    }

    public String getLogUrl() {
        return logUrl;
    }

    public void setLogUrl(String logUrl) {
        this.logUrl = logUrl;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "JenkinsBuild{" +
                "duration=" + duration +
                ", number=" + number +
                ", result='" + result + '\'' +
                ", url='" + url + '\'' +
                '}';
    }


    public boolean isSuccessful() {
        if (this.result.equalsIgnoreCase("SUCCESS"))
            return true;
        else if (this.result.equalsIgnoreCase("FAILURE"))
            return false;
        else
            throw new IllegalStateException(result + " is not a valid result for a JenkinsBuild. Expected \"SUCCESS\" or \"FAILURE\"");
    }
}
