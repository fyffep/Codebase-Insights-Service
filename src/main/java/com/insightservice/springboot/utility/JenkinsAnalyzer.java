package com.insightservice.springboot.utility;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.insightservice.springboot.model.JenkinsBuild;
import com.insightservice.springboot.model.codebase.Codebase;
import com.insightservice.springboot.model.codebase.FileObject;
import com.insightservice.springboot.model.codebase.HeatObject;
import com.insightservice.springboot.utility.commit_history.JGitHelper;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.IOException;
import java.util.*;

import static com.insightservice.springboot.Constants.LOG;


public class JenkinsAnalyzer
{
    private static ObjectMapper objectMapper = new ObjectMapper();

    //TEMP
    static String username = "";
    static String apiKey = "";
    static String jenkinsHost = "";
    static String jobName = "";

    //TEMPORARY to help me test
    public Codebase getDummyCodebase(String branchName) throws GitAPIException, IOException
    {
        String remoteUrl = "https://github.com/fyffep/P565-SP21-Patient-Manager";

        //Obtain file metrics by analyzing the code base
        RepositoryAnalyzer repositoryAnalyzer = null;
        try
        {
            JGitHelper.cloneRepository(remoteUrl, branchName);
            Codebase codebase = new Codebase();

            //Calculate file sizes for every commit
            repositoryAnalyzer = new RepositoryAnalyzer(remoteUrl);
            RepositoryAnalyzer.attachBranchNameList(codebase);
            codebase.selectDefaultBranch();
            RepositoryAnalyzer.attachCodebaseData(codebase);

            //Now the Codebase contains all the data it needs
            LOG.info("Heat calculations complete. Number of files: " + codebase.getActiveFileObjects().size());

            return codebase;
        }
        catch (IOException | GitAPIException e) {
            e.printStackTrace();
            LOG.error(e.toString());
            LOG.error(e.getMessage());

            throw e;
        }
        finally
        {
            //Close the .git files
            if (repositoryAnalyzer != null)
                repositoryAnalyzer.cleanup();
            JGitHelper.removeClonedRepository(remoteUrl);
        }
    }


    /**
     * @param buildNumber the Jenkins build number
     * @param branchName the name of the GitHub branch that the Jenkins user initiated the build for.
     * @return the GitHub commit hash that the Jenkins build ran on OR null if the branch
     * was not used for that commit.
     * @throws JsonProcessingException
     */
    private static String getCommitHashFromBuildNumberAndBranchName(int buildNumber, String branchName) throws JsonProcessingException
    {
        //Parse specific build
        WebClient client = WebClient.create(jenkinsHost);
        String response = client.get()
                .uri(String.format("job/P565-SP21-Patient-Manager/%d/api/json", buildNumber))
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


    //UNUSED: This doesn't always point to the right commit hash. It just ensures the branch is correct.
    //Can be deleted.
//    public static String getCommitHashFromBuildNumber(int buildNumber, String branchName) throws JsonProcessingException
//    {
//        //Parse specific build
//        WebClient client = WebClient.create(jenkinsHost);
//        String response = client.get()
//                .uri(String.format("job/P565-SP21-Patient-Manager/%d/api/json", buildNumber))
//                .headers(headers -> headers.setBasicAuth(username, apiKey))
//                .retrieve()
//                .bodyToMono(String.class)
//                .block();
//
//        ObjectMapper objectMapper = new ObjectMapper();
//        final ObjectNode root = objectMapper.readValue(response, ObjectNode.class);
//        JsonNode actionsArray = root.get("actions");
//        JsonNode action1 = actionsArray.get(1);
//        JsonNode buildsByBranchNameArray = action1.get("buildsByBranchName");
//        Iterator<String> branchNameIterator = buildsByBranchNameArray.fieldNames();
//        while (branchNameIterator.hasNext())
//        {
//            String branchNameInJson = branchNameIterator.next();
//            System.out.println("branchJson = "+branchNameInJson);
//            if (branchNameInJson.toString().endsWith(branchName))
//            {
//                JsonNode buildJson = buildsByBranchNameArray.get(branchNameInJson);
//                JsonNode markedJson = buildJson.get("marked");
//
//                return markedJson.get("SHA1").toString();
//            }
//        }
//
//        throw new BadBranchException("The branch " + branchName + " was not used for build #" + buildNumber);
//    }


    /**
     * Returns the GitHub commit hash that was used to trigger the Jenkins build.
     * If the build did not occur on the target branch, return <code> null. </code>
     */
    private static String getCommitHashFromBuildNumber(int buildNumber, String branchName) throws JsonProcessingException
    {
        //Parse specific build
        WebClient client = WebClient.create(jenkinsHost);
        String response = client.get()
                .uri(String.format("job/P565-SP21-Patient-Manager/%d/api/json", buildNumber))
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
    private static List<JenkinsBuild> getListOfRecentBuilds(int maxCount) throws IOException
    {
        if (maxCount < 1)
            throw new IllegalArgumentException("maxCount must be at least one");
        //FIXME adding maxCount to the uri causes an exception

        WebClient client = WebClient.create(jenkinsHost);
        String response = client.get()
                .uri("/api/json?tree=jobs[name,url,builds[number,result,duration,url]]")
                .headers(headers -> headers.setBasicAuth(username, apiKey))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        List<JenkinsBuild> jenkinsBuildList = new ArrayList<>();

        final ObjectNode root = objectMapper.readValue(response, ObjectNode.class);
        final JsonNode jobsArrayJson = root.get("jobs");
        final JsonNode job0 = jobsArrayJson.get(0);

        JsonNode buildArrayJson = job0.get("builds");
        for (JsonNode buildNode : buildArrayJson)
        {
            ((ObjectNode) buildNode).remove("_class"); //allows us to convert to JenkinsBuild class
            JenkinsBuild jenkinsBuild = objectMapper.readValue(buildNode.toString(), JenkinsBuild.class);

            System.out.printf("Jenkins build: `%s`\n", jenkinsBuild.toString());
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

    private static void requestAndStoreConsoleData(int buildNumber, Codebase codebaseToModify, String commitHash)
    {
        Set<FileObject> fileObjectSet = codebaseToModify.getActiveFileObjectsExcludeDeletedFiles(commitHash);

        WebClient client = WebClient.create(jenkinsHost);
        final String consoleUrl = "/job/%s/%d/logText/progressiveText?start=0";
        try {
            String response = client.get()
                    .uri(String.format(consoleUrl, jobName, buildNumber))
                    .headers(headers -> headers.setBasicAuth(username, apiKey))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            HashSet<String> filesInStackTrace = extractFileNamesFromConsoleOutput(response);

            //Store activity in HeatObjects
            for (String fileName : filesInStackTrace) {
                LOG.info(fileName + " has activity on build #" + buildNumber);

                FileObject fileObject = codebaseToModify.getFileObjectFromFilename(fileName);
                if (fileObject != null)
                {
                    LOG.info(fileName +" is a member of our codebase");

                    HeatObject heatObject = fileObject.createOrGetHeatObjectAtCommit(commitHash);
                    int buildHeat = heatObject.getGoodBadCommitRatioHeat();
                    heatObject.setGoodBadCommitRatioHeat(buildHeat + 1);
                    LOG.info(fileName+" now has heat "+heatObject.getGoodBadCommitRatioHeat() +" at commit "+ commitHash);
                }
            }
        }
        //FIXME sometimes the output is too large...usually because of a successful build
        catch (WebClientResponseException ex) {
            LOG.error("Couldn't analyze build #" + buildNumber);
        }
    }




    public static void attachJenkinsStackTraceActivityToCodebase(Codebase codebase) throws IOException
    {
        //Get all the recent builds. The build numbers could be noncontiguous, like 18, 16, 15, 14, 10, 9
        final int NUMBER_OF_BUILDS_TO_CHECK = 50;
        int remainingBuildsToCheck = NUMBER_OF_BUILDS_TO_CHECK;
        List<JenkinsBuild> recentBuildList = getListOfRecentBuilds(Integer.MAX_VALUE); //not sure if it's an issue to get every single build
        for (JenkinsBuild jenkinsBuild : recentBuildList)
        {
            if (!jenkinsBuild.isSuccessful()) //if build failed
            {
                //Determine which commit hash caused the build failure
                int buildNumber = jenkinsBuild.getNumber();
                String commitHashOfBuild = getCommitHashFromBuildNumber(buildNumber, codebase.getActiveBranch());

                if (commitHashOfBuild != null)
                {
                    //Find which files appeared in the stack trace at that build, then increment their counter in the Codebase
                    requestAndStoreConsoleData(buildNumber, codebase, commitHashOfBuild);

                    remainingBuildsToCheck--;
                    if (remainingBuildsToCheck <= 0)
                        return;
                }
                //Else, the target branch was not used for the build, so don't count the build.
            }
        }
    }


    public static void main(String[] args) throws IOException, GitAPIException
    {
        //Set up Codebase
        JenkinsAnalyzer jenkinsTestMain = new JenkinsAnalyzer();
        Codebase codebase = jenkinsTestMain.getDummyCodebase("intentional-bugs");

        attachJenkinsStackTraceActivityToCodebase(codebase);
    }
}
