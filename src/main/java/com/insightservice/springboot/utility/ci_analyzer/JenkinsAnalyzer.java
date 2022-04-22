package com.insightservice.springboot.utility.ci_analyzer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.insightservice.springboot.exception.BadUrlException;
import com.insightservice.springboot.model.CIBuild;
import com.insightservice.springboot.model.codebase.Codebase;
import com.insightservice.springboot.model.codebase.Commit;
import com.insightservice.springboot.model.codebase.FileObject;
import com.insightservice.springboot.model.codebase.HeatObject;
import org.apache.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.*;

import static com.insightservice.springboot.Constants.LOG;
import static com.insightservice.springboot.Constants.SCORE_PENALTY_AT_BUILD_FAILURE;


public class JenkinsAnalyzer
{
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * @param buildNumber the Jenkins build number
     * @param branchName the name of the GitHub branch that the Jenkins user initiated the build for.
     * @return the GitHub commit hash that the Jenkins build ran on OR null if the branch
     * was not used for that commit.
     * @throws JsonProcessingException
     */
    private static String getCommitHashFromBuildNumberAndBranchName(int buildNumber, String branchName, String username, String apiKey, String jobUrl) throws JsonProcessingException
    {
        //Parse specific build
        WebClient client = WebClient.create(jobUrl);
        String response = client.get()
                .uri(String.format("/%d/api/json", buildNumber))
                .headers(headers -> headers.setBasicAuth(username, apiKey))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        ObjectMapper objectMapper = new ObjectMapper();
        final ObjectNode root = objectMapper.readValue(response, ObjectNode.class);
        JsonNode actionsArray = root.get("actions");
        JsonNode action1 = actionsArray.get(1);
        JsonNode buildsByBranchNameArray = action1.get("buildsByBranchName");
        Iterator<String> branchNameIterator = buildsByBranchNameArray.fieldNames();
        while (branchNameIterator.hasNext())
        {
            String branchNameInJson = branchNameIterator.next();
            if (branchNameInJson.toString().endsWith(branchName))
            {
                JsonNode buildJson = buildsByBranchNameArray.get(branchNameInJson);
                JsonNode markedJson = buildJson.get("marked");

                return markedJson.get("SHA1").toString();
            }
        }

        //The build used a different branch than the one being analyzed
        LOG.info("The branch " + branchName + " was not used for build #" + buildNumber);
        return null;
    }


    /**
     * Returns the GitHub commit hash that was used to trigger the Jenkins build.
     * If the build did not occur on the target branch, return <code> null. </code>
     */
    private static String getCommitHashFromBuildNumber(int buildNumber, String branchName, String username, String apiKey, String jobUrl) throws JsonProcessingException
    {
        //Parse specific build
        WebClient client = WebClient.create(jobUrl);
        String response = client.get()
                .uri(String.format("/%d/api/json", buildNumber))
                .headers(headers -> headers.setBasicAuth(username, apiKey))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        ObjectMapper objectMapper = new ObjectMapper();
        final ObjectNode root = objectMapper.readValue(response, ObjectNode.class);
        JsonNode actionsArray = root.get("actions");
        JsonNode action1 = actionsArray.get(1);
        JsonNode lastBuiltRevision = action1.get("lastBuiltRevision");
        JsonNode branchArray = lastBuiltRevision.get("branch");
        for (JsonNode branchJson : branchArray)
        {
            String branchNameInJson = branchJson.get("name").asText();
            if (branchNameInJson.endsWith(branchName))
            {
                return branchJson.get("SHA1").toString();
            }
        }

        //Valid data, but the build didn't belong to that branch
        LOG.info("The branch " + branchName + " was not used for build #" + buildNumber);
        return null;
    }


    /**
     * Requests the most recent builds from Jenkins and stores them in a list of JenkinsBuilds.
     * @param maxCount the inclusive maximum number of most recent builds to fetch
     */
    private static List<CIBuild> getListOfRecentBuilds(int maxCount, String username, String apiKey, String jobUrl) throws IOException, WebClientRequestException
    {
        if (maxCount < 1)
            throw new IllegalArgumentException("maxCount must be at least one");
        //FIXME adding maxCount to the uri causes an exception

        String jenkinsHost = getJenkinsHostFromJobUrl(jobUrl);
        WebClient client = WebClient.create(jenkinsHost);
        String response = client.get()
                .uri("/api/json?tree=jobs[name,url,builds[number,result,duration,url]]")
                .headers(headers -> headers.setBasicAuth(username, apiKey))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        List<CIBuild> jenkinsBuildList = new ArrayList<>();

        final ObjectNode root = objectMapper.readValue(response, ObjectNode.class);
        final JsonNode jobsArrayJson = root.get("jobs");
        final JsonNode job0 = jobsArrayJson.get(0);

        JsonNode buildArrayJson = job0.get("builds");
        for (JsonNode buildNode : buildArrayJson) {
            ((ObjectNode) buildNode).remove("_class"); //allows us to convert to JenkinsBuild class
            CIBuild jenkinsBuild = objectMapper.readValue(buildNode.toString(), CIBuild.class);

            //System.out.printf("Jenkins build: `%s`\n", jenkinsBuild.toString());
            jenkinsBuildList.add(jenkinsBuild);
        }

        return jenkinsBuildList;
    }



    private static HashSet<String> extractFileNamesFromConsoleOutput(String consoleOutput)
    {
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

    private static void requestAndStoreConsoleData(int buildNumber, Codebase codebaseToModify, String commitHash, String username, String apiKey, String jobUrl)
    {
        Set<FileObject> fileObjectSet = codebaseToModify.getActiveFileObjectsExcludeDeletedFiles(commitHash);

        String jenkinsHost = getJenkinsHostFromJobUrl(jobUrl);
        WebClient client = WebClient.create(jenkinsHost);
        try {
            String response = client.get()
                    .uri(String.format("%s/%d/logText/progressiveText?start=0", jobUrl, buildNumber))
                    .headers(headers -> headers.setBasicAuth(username, apiKey))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            HashSet<String> filesInStackTrace = extractFileNamesFromConsoleOutput(response);

            //Store activity in HeatObjects
            for (String fileName : filesInStackTrace) {

                FileObject fileObject = codebaseToModify.getFileObjectFromFilename(fileName);
                if (fileObject != null) {
                    LOG.info(fileName + " has activity on build #" + buildNumber + " (commit " + commitHash + ")");
                    //Increment scores
                    commitHash = commitHash.replaceAll("\"", "");
                    penalizeBuildFailureScores(fileObject, commitHash);
                }
            }
        }
        //FIXME sometimes the output is too large...usually because of a successful build
        catch (WebClientResponseException ex) {
            LOG.error("Couldn't analyze build #" + buildNumber);
        }
    }

    private static void penalizeBuildFailureScores(FileObject fileObject, String commitHashOfFailure)
    {
        boolean foundHash = false;
        int currentPenalty = SCORE_PENALTY_AT_BUILD_FAILURE;
        for (Map.Entry<String, HeatObject> entry : fileObject.getCommitHashToHeatObjectMap().entrySet())
        {
            //Check if this is the commit the build failure occurred on.
            if (!foundHash && entry.getKey().equals(commitHashOfFailure))
                foundHash = true; //Now we know all subsequent commits are on or after the build failure.

            //For each commit on and after the build failure
            if (foundHash)
            {
                //Incur currentPenalty points
                HeatObject heatObject = fileObject.getHeatObjectAtCommit(entry.getKey());
                if (heatObject == null)
                {
                    LOG.error("Jenkins found that file `"+fileObject.getPath()+"` appeared at commit `"+entry.getKey()+"`, but we had no HeatObject for it. Ignoring build failure.");
                    continue;
                }
                heatObject.setBuildFailureScore(heatObject.getBuildFailureScore() + currentPenalty);

                if (currentPenalty > 1)
                    currentPenalty--;
            }
        }
    }

    private static String getJenkinsHostFromJobUrl(String jobUrl)
    {
        try
        {
            return jobUrl.split("/job")[0];
        }
        catch (IndexOutOfBoundsException ex)
        {
            throw new BadUrlException("Could not read the Jenkins URL correctly due to it not having the word 'job");
        }
    }




    public static void attachJenkinsStackTraceActivityToCodebase(Codebase codebase, String username, String apiKey, String jobUrl) throws IOException
    {
        try
        {
            //Get all the recent builds. The build numbers could be noncontiguous, like 18, 16, 15, 14, 10, 9
            final int NUMBER_OF_BUILDS_TO_CHECK = 50;
            int remainingBuildsToCheck = NUMBER_OF_BUILDS_TO_CHECK;
            List<CIBuild> recentBuildList = getListOfRecentBuilds(Integer.MAX_VALUE, username, apiKey, jobUrl);
            for (CIBuild jenkinsBuild : recentBuildList) {
                if (!jenkinsBuild.isSuccessful()) //if build failed
                {
                    //Determine which commit hash caused the build failure
                    int buildNumber = jenkinsBuild.getNumber();
                    String commitHashOfBuild = getCommitHashFromBuildNumber(buildNumber, codebase.getActiveBranch(), username, apiKey, jobUrl);

                    if (commitHashOfBuild != null) {
                        //Find which files appeared in the stack trace at that build, then increment their counter in the Codebase
                        requestAndStoreConsoleData(buildNumber, codebase, commitHashOfBuild, username, apiKey, jobUrl);

                        remainingBuildsToCheck--;
                        if (remainingBuildsToCheck <= 0)
                            return;
                    }
                    //Else, the target branch was not used for the build, so don't count the build.
                }
            }
        }
        catch (WebClientRequestException ex)
        {
            if (ex.getCause() instanceof UnknownHostException) {
                throw new UnknownHostException("The Jenkins URL is invalid.");
            }
            throw ex; //else, idk what happened
        }
        catch (WebClientResponseException ex)
        {
            if (ex.getRawStatusCode() == HttpStatus.SC_UNAUTHORIZED) { //401 error
                throw new BadUrlException("The Jenkins job name is invalid, or your API key does not grant you permission to access the job.");
            }
            throw ex; //else, idk what happened
        }
    }
}