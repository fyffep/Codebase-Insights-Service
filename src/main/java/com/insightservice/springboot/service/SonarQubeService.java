package com.insightservice.springboot.service;

import com.google.gson.Gson;
import com.insightservice.springboot.model.sonar.Issue;
import com.insightservice.springboot.model.sonar.IssueResponse;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

import static com.insightservice.springboot.Constants.LOG;

public class SonarQubeService {


    //TODO read via ENV variables
    String sonarUrl = "http://sonar-qube-vampire.eastus.cloudapp.azure.com:9000";
    String sonarToken = "c9afe9fb9254036fd7c960bdfab114cd5d17261b";
    String authHeader = "Basic YzlhZmU5ZmI5MjU0MDM2ZmQ3Yzk2MGJkZmFiMTE0Y2Q1ZDE3MjYxYjo=";

    private SonarQubeService() {
    }

    private static SonarQubeService instance;

    public static SonarQubeService getInstance() {
        if (instance == null) {
            instance = new SonarQubeService();
        }
        return instance;
    }

    /**
     * Get all issues corresponding to a project in SonarQube
     * @param project Project ID on SonarQube
     * @return
     * @throws IOException
     */
    public Issue[] getIssues(String project) throws IOException {

        //TODO handle pagination, i.e. the call returns only the top 100 issues. Need to make additional calls to fetch all the issues data
        if(project == null) {
            return null;
        }

        CloseableHttpClient httpClient = HttpClients.createDefault();

        try {

            HttpGet httpGet = new HttpGet(sonarUrl + "/api/issues/search?componentKeys=" + project);
            httpGet.addHeader("Authorization", authHeader);

            CloseableHttpResponse response =  httpClient.execute(httpGet);

            try {
                HttpEntity entity = response.getEntity();
                String responseJson = EntityUtils.toString(entity);

                Gson gson = new Gson();
                IssueResponse issueResponse = gson.fromJson(responseJson, IssueResponse.class);

                return issueResponse.issues;
            }
            catch (Exception e) {
                LOG.error("SonarQubeService::getIssues -> Error while parsing response " + e);
            }
            finally {
                response.close();
            }

        } catch(Exception e) {
            LOG.error("SonarQubeService::getIssues -> Could not make call to Sonar service");

        } finally {
            httpClient.close();
        }
        return null;
    }

    public static void main(String[] args) {
        try{
            SonarQubeService.getInstance().getIssues("patient-manager");
        } catch (IOException e) {
            System.out.println(e.toString());
        }
    }
}
