package com.insightservice.springboot.utility;

import com.insightservice.springboot.model.codebase.Codebase;
import com.insightservice.springboot.model.codebase.FileObject;
import com.insightservice.springboot.model.codebase.HeatObject;
import com.insightservice.springboot.utility.commit_history.JGitHelper;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static com.insightservice.springboot.Constants.LOG;


public class JenkinsAnalyzer
{
    //TEMPORARY to help me test
    public Codebase getDummyCodebase() throws GitAPIException, IOException
    {
        String remoteUrl = "https://github.com/fyffep/P565-SP21-Patient-Manager";

        //Obtain file metrics by analyzing the code base
        RepositoryAnalyzer repositoryAnalyzer = null;
        try
        {
            JGitHelper.cloneRepository(remoteUrl, "master");
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







    public static HashSet<String> extractFileNamesFromConsoleOutput(String consoleOutput)
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

                System.out.println("Added "+s);
                filesInStackTrace.add(s);
            }
        }

        return filesInStackTrace;
    }

    public static void requestAndStoreConsoleData(Codebase codebaseToModify)
    {
        //TODO change to proper commit hash
        String commitHash = "79732b0439199d7ec2c76db4b209b4466c3fa835";
        int buildFailureCount = 14; //TEMP
        Set<FileObject> fileObjectSet = codebaseToModify.getActiveFileObjectsExcludeDeletedFiles(commitHash);

        WebClient client = WebClient.create("https://codebase-insights-rawlins-vampire.snowy.luddy.indiana.edu");
        String consoleUrl = "/job/P565-SP21-Patient-Manager/%d/logText/progressiveText?start=0";
        for (int i = 1; i <= buildFailureCount; i++)
        {
            try {
                String response = client.get()
                        .uri(String.format(consoleUrl, i))
                        .headers(headers -> headers.setBasicAuth())
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                HashSet<String> filesInStackTrace = extractFileNamesFromConsoleOutput(response);

                //Store activity in HeatObjects
                for (String fileName : filesInStackTrace) {
                    System.out.println("" + fileName + " has activity on build #" + i);

                    FileObject fileObject = codebaseToModify.getFileObjectFromFilename(fileName);
                    if (fileObject != null)
                    {
                        System.out.println(""+fileName +" is a member of our codebase");

                        HeatObject heatObject = fileObject.createOrGetHeatObjectAtCommit(commitHash);
                        int buildHeat = heatObject.getGoodBadCommitRatioHeat();
                        heatObject.setGoodBadCommitRatioHeat(buildHeat + 1);
                        System.out.println(""+fileName+" now has heat "+heatObject.getGoodBadCommitRatioHeat() +" at commit "+ commitHash);
                    }
                }
            }
            //FIXME sometimes the output is too large
            catch (WebClientResponseException ex) {
                System.out.println("Couldn't analyze build #"+i);
                continue;
            }
        }
    }

    public static void main(String[] args) throws IOException, GitAPIException
    {
        //Parse specific build
        int buildNumber = 2;
//        WebClient client = WebClient.create("https://codebase-insights-rawlins-vampire.snowy.luddy.indiana.edu");
//        String response = client.get()
//                .uri(String.format("job/P565-SP21-Patient-Manager/%d/api/json", buildNumber))
//                //.header("Authorization", credential)
//                .headers(headers -> headers.setBasicAuth("username", /*TODO remove*/ "pass"))
//                //.accept(MediaType.APPLICATION_JSON)
//                //.body(BodyInserters.fromFormData(bodyValues))
//                .retrieve()
//                .bodyToMono(String.class)
//                .block();
//
//        ObjectMapper objectMapper = new ObjectMapper();
//        final ObjectNode root = objectMapper.readValue(response, ObjectNode.class);
//        System.out.println("fullDisplayName = " +root.get("fullDisplayName"));
//
//        JsonNode arr = root.get("actions");
//        for (JsonNode node : arr)
//        {
//            System.out.println("node is "+node);
//        }
//
//        System.out.println("\n\n\n\n\n\n");
//        extractConsoleOutput();

        JenkinsAnalyzer jenkinsTestMain = new JenkinsAnalyzer();
        Codebase codebase = jenkinsTestMain.getDummyCodebase();
        requestAndStoreConsoleData(codebase);

        //ObjectMapper objectMapper = new ObjectMapper();
        //System.out.println("\nFINAL OUTPUT:"+objectMapper.writeValueAsString(codebase));
    }
}
