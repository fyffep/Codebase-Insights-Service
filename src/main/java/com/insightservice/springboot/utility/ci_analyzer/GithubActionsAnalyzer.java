package com.insightservice.springboot.utility.ci_analyzer;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.insightservice.springboot.exception.BadUrlException;
import com.insightservice.springboot.model.CIBuild;
import com.insightservice.springboot.model.codebase.Codebase;
import com.insightservice.springboot.model.codebase.FileObject;
import com.insightservice.springboot.model.codebase.HeatObject;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import org.apache.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static com.insightservice.springboot.Constants.LOG;

public class GithubActionsAnalyzer {
    private static final String GITHUB_ACTIONS_URL_FORMAT_ALL = "https://%s/api/v3/repos/%s/actions/workflows";
    private static final String GITHUB_ACTIONS_URL_FORMAT_SPECIFIC = "https://%s/api/v3/repos/%s/actions/workflows/%d/runs";
    //Example https://github.iu.edu/P532-Fall2021/codebase-insights-service

    public static void attachBuildData(Codebase codebase, String remoteUrl, String oAuthToken) throws IOException {
        try {
            final int NUMBER_OF_BUILDS_TO_CHECK = 50;
            int remainingBuildsToCheck = NUMBER_OF_BUILDS_TO_CHECK;
            List<CIBuild> recentBuildList = getRecentBuilds(remoteUrl, oAuthToken);
            for (CIBuild ciBuild : recentBuildList) {
                if (!ciBuild.isSuccessful()) { // Check if build failed
                    //Determine which commit hash caused the build failure
                    int buildNumber = ciBuild.getNumber();
                    String commitHashOfBuild = ciBuild.getCommitHash();

                    if (commitHashOfBuild != null) {
                        //Find which files appeared in the stack trace at that build, then increment their counter in the Codebase
                        // TODO: Fix GitHubActions
                        requestAndStoreConsoleData(buildNumber, ciBuild.getLogUrl(), oAuthToken, codebase, commitHashOfBuild);

                        remainingBuildsToCheck--;
                        if (remainingBuildsToCheck <= 0)
                            return;
                    } //Else, the target branch was not used for the build, so don't count the build.
                }
            }
        } catch (WebClientRequestException ex) {
            if (ex.getCause() instanceof UnknownHostException) {
                throw new UnknownHostException("The GitHub Actions URL is invalid.");
            }
        } catch (WebClientResponseException ex) {
            if (ex.getRawStatusCode() == HttpStatus.SC_UNAUTHORIZED) { //401 error
                throw new BadUrlException("The GitHub Actions job name is invalid, or your Auth token does not grant you permission to access the job.");
            }
        }
    }

    private static void requestAndStoreConsoleData(int buildNumber, String logUrl, String oAuthToken, Codebase codebaseToModify, String commitHash) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(logUrl)
                .method("GET", null)
                .addHeader("Authorization", String.format("Bearer %s", oAuthToken))
                .build();
        try {
            String response = client.newCall(request).execute().body().string();

            HashSet<String> filesInStackTrace = extractFileNamesFromConsoleOutput(response);

            //Store activity in HeatObjects
            for (String fileName : filesInStackTrace) {
                LOG.info(fileName + " has activity on build #" + buildNumber);

                FileObject fileObject = codebaseToModify.getFileObjectFromFilename(fileName);
                if (fileObject != null) {
                    LOG.info(fileName +" is a member of our codebase");

                    HeatObject heatObject = fileObject.createOrGetHeatObjectAtCommit(commitHash);
                    int buildHeat = heatObject.getGoodBadCommitRatioHeat();
                    heatObject.setGoodBadCommitRatioHeat(buildHeat + 1);
                    LOG.info(fileName+" now has heat "+heatObject.getGoodBadCommitRatioHeat() +" at commit "+ commitHash);
                }
            }
        }
        //FIXME sometimes the output is too large...usually because of a successful build
        catch (WebClientResponseException | IOException ex) {
            LOG.error("Couldn't analyze build #" + buildNumber);
        }
    }

    private static HashSet<String> extractFileNamesFromConsoleOutput(String consoleOutput) {
        final HashSet<String> filesInStackTrace = new HashSet<>();
        final String javaExtension = ".java";

        for (String s : consoleOutput.split("/")) //FIXME maybe not every stack trace has forward slashes.
        {
            if (s.contains(javaExtension))
            {
                s = s.substring(0, s.indexOf(javaExtension) + javaExtension.length());
                s = s.trim();

                //Exclude the symbol immediately before the file name as well as everything before that symbol.
                //Ex: "(some-text)MyApp.java" produces ["some", "text", "MyApp", "java"]
                String[] splitBySymbol = s.split("[^a-zA-Z0-9]");
                assert splitBySymbol.length >= 2;
                s = splitBySymbol[splitBySymbol.length - 2] + "." + splitBySymbol[splitBySymbol.length - 1];

                filesInStackTrace.add(s);
            }
        }

        return filesInStackTrace;
    }

    private static List<CIBuild> getRecentBuilds(String remoteUrl, String oAuthToken) throws MalformedURLException {
        String githubOrgDNS  = getGitHubOrgDNS(remoteUrl);
        String githubRepoName = getGitHubRepoName(remoteUrl);
        int workFlowId  = getWorkFlowId(githubOrgDNS, githubRepoName, oAuthToken);
        List<CIBuild> recentBuilds = getRecentBuilds(githubOrgDNS, githubRepoName, workFlowId, oAuthToken);
        return recentBuilds;
    }

    private static List<CIBuild> getRecentBuilds(String githubOrgDNS, String githubRepoName, int workFlowId, String oAuthToken) {
        List<CIBuild> ciBuilds = new ArrayList<>();

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(String.format(GITHUB_ACTIONS_URL_FORMAT_SPECIFIC, githubOrgDNS, githubRepoName, workFlowId))
                .method("GET", null)
                .addHeader("Authorization", String.format("Bearer %s", oAuthToken))
                .build();
        try {
            Response response = client.newCall(request).execute();
            JsonObject jsonObject = JsonParser.parseString(response.body().string()).getAsJsonObject();
            JsonArray jsonArray = jsonObject.getAsJsonArray("workflow_runs");
            jsonArray.forEach(workflowRun -> {
                CIBuild ciBuild = new CIBuild();
                JsonObject jsonObj = workflowRun.getAsJsonObject();
                ciBuild.setNumber(jsonObj.get("run_number").getAsInt());
                ciBuild.setResult(jsonObj.get("conclusion").getAsString().toUpperCase());
                ciBuild.setUrl(jsonObj.get("url").getAsString());
                ciBuild.setCommitHash(jsonObj.get("head_sha").getAsString());
                ciBuild.setLogUrl(jsonObj.get("logs_url").getAsString());
                ciBuilds.add(ciBuild);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ciBuilds;
    }

    private static int getWorkFlowId(String githubOrgDNS, String githubRepoName, String oAuthToken) {
        OkHttpClient client = new OkHttpClient();
        String url = String.format(GITHUB_ACTIONS_URL_FORMAT_ALL, githubOrgDNS, githubRepoName);
        Request request = new Request.Builder()
                .url(url)
                .method("GET", null)
                .addHeader("Authorization", String.format("Bearer %s", oAuthToken))
                .build();
        try {
            Response response = client.newCall(request).execute();
            JsonObject jsonObject = JsonParser.parseString(response.body().string()).getAsJsonObject();
            JsonArray jsonArray = jsonObject.getAsJsonArray("workflows");
            return jsonArray.get(0).getAsJsonObject().get("id").getAsInt();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return -1;
    }

    private static String getGitHubOrgDNS(String remoteUrl) {
        return remoteUrl.split("/")[2]; // Eg: https://github.iu.edu/
    }

    private static String getGitHubRepoName(String remoteUrl) {
        return remoteUrl.substring(remoteUrl.indexOf(remoteUrl.split("/")[3] + "/"));
    }
}
