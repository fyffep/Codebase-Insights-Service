package com.insightservice.springboot.model;

public class JenkinsBuild
{
    private long duration;
    private int number;
    private String result; //"SUCCESS" or "FAILURE"
    private String url; //e.g. "https://<JENKINS HOST>/job/<JOB NAME>/<BUILD NUMBER>/"

    public JenkinsBuild() {
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
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


    public boolean isSuccessful()
    {
        if (this.result.equals("SUCCESS"))
            return true;
        else if (this.result.equals("FAILURE"))
            return false;
        else
            throw new IllegalStateException(result + " is not a valid result for a JenkinsBuild. Expected \"SUCCESS\" or \"FAILURE\"");
    }
}
