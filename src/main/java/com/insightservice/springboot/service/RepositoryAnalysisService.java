package com.insightservice.springboot.service;

import com.insightservice.springboot.model.codebase.Codebase;
import com.insightservice.springboot.model.codebase.Commit;
import com.insightservice.springboot.model.codebase.FileObject;
import com.insightservice.springboot.payload.SettingsPayload;
import com.insightservice.springboot.repository.CodebaseRepository;
import com.insightservice.springboot.repository.CommitRepository;
import com.insightservice.springboot.repository.FileObjectRepository;
import com.insightservice.springboot.utility.GroupFileObjectUtility;
import com.insightservice.springboot.utility.HeatCalculationUtility;
import com.insightservice.springboot.utility.ci_analyzer.GithubActionsAnalyzer;
import com.insightservice.springboot.utility.ci_analyzer.JenkinsAnalyzer;
import com.insightservice.springboot.utility.RepositoryAnalyzer;
import com.insightservice.springboot.utility.commit_history.JGitHelper;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static com.insightservice.springboot.Constants.*;

@Service
public class RepositoryAnalysisService
{
    @Autowired
    CodebaseRepository codebaseRepository;
    @Autowired
    FileObjectRepository fileObjectRepository;


    public Codebase getOrCreateCodebase(SettingsPayload settingsPayload) throws GitAPIException, IOException
    {
        if (settingsPayload == null)
            throw new IllegalArgumentException("SettingsPayload cannot be null.");

        Codebase codebase = codebaseRepository.findById(settingsPayload.getGithubUrl()).orElse(null);
        //If codebase is new OR
        //if branch changed OR
        //if codebase is outdated due to a new commit
        if (codebase == null ||
                (!codebase.getActiveBranch().equals(settingsPayload.getBranchName()) && !settingsPayload.getBranchName().equals(USE_DEFAULT_BRANCH)) ||
                !JGitHelper.checkIfLatestCommitIsUpToDate(codebase, settingsPayload.getGithubOAuthToken()))
        {
            LOG.info("Beginning new Codebase analysis because the repo is new or updated...");
            codebase = extractDataToCodebase(settingsPayload.getGithubUrl(), settingsPayload.getBranchName(), settingsPayload.getGithubOAuthToken());
            LOG.info("Finished Codebase analysis.");
        }
        //Else, up-to-date codebase data exists
        else {
            LOG.info("Returning old Codebase data because the repo is up-to-date.");
        }

        //Always run CI since we have no logic to check for updates.
        //3rd-party CI tool analysis for build failures
        this.runCiAnalysis(codebase, settingsPayload);

        return codebase;
    }

    /**
     * Clones a GitHub repos into local memory, then analyzes it.
     * All data is placed into a Codebase.
     * Finally, the local repos is deleted from the file system.
     * @param remoteUrl the URL to the home page of a user's GitHub repository
     * @param oauthToken
     * @return the Codebase containing all heat and file data.
     */
    private Codebase extractDataToCodebase(String remoteUrl, String branchName, String oauthToken) throws GitAPIException, IOException
    {
        //Obtain file metrics by analyzing the code base
        RepositoryAnalyzer repositoryAnalyzer = null;
        try
        {
            JGitHelper.cloneOrUpdateRepository(remoteUrl, branchName, oauthToken);
            Codebase codebase = new Codebase();
            codebase.setGitHubUrl(remoteUrl);

            //Calculate heat metrics for every commit
            repositoryAnalyzer = new RepositoryAnalyzer(remoteUrl);
            RepositoryAnalyzer.attachBranchNameList(codebase);
            codebase.selectDefaultBranch();
            LOG.info("Running RepositoryAnalyzer.attachCodebaseData(...)...");
            RepositoryAnalyzer.attachCodebaseData(codebase);
            GroupFileObjectUtility.groupByCommit(codebase); //this sets the degree of external coupling for each file

            //TODO run SonarQube here

            HeatCalculationUtility.assignHeatLevels(codebase);

            //Now the Codebase contains all the data it needs
            LOG.info("Heat calculations complete. Number of files: " + codebase.getActiveFileObjects().size());

            //Persist the codebase
            saveCodebase(codebase);

            return codebase;
        }
        finally
        {
            //Close the .git files
            if (repositoryAnalyzer != null)
                repositoryAnalyzer.cleanup();
        }
    }

    public void runCiAnalysis(Codebase codebase, SettingsPayload settingsPayload) throws IOException {
        String remoteUrl = settingsPayload.getGithubUrl();
        String ciToolChosen = settingsPayload.getCiToolChosen();
        String oAuthToken = settingsPayload.getGithubOAuthToken();

        if (ciToolChosen.equals(JENKINS)) {
            //Analyze Jenkins data
            LOG.info("Beginning Jenkins analysis of the repository with URL `"+ remoteUrl +"`...");
            this.attachJenkinsData(codebase, settingsPayload.getCiUsername(), settingsPayload.getApiKey(), settingsPayload.getJobUrl());
            System.out.println("Done! Latest hash="+codebase.getLatestCommitHash()+" heatobj: "+codebase.getFileObjectFromFilename("PatientManagerSpringApplication.java").createOrGetHeatObjectAtCommit(codebase.getLatestCommitHash()) +" score="+codebase.getFileObjectFromFilename("PatientManagerSpringApplication.java").createOrGetHeatObjectAtCommit(codebase.getLatestCommitHash()));
            LOG.info("Finished Jenkins analysis for the repository with URL `"+ remoteUrl +"`.");
        }
        else if (ciToolChosen.equals(GITHUB_ACTIONS)) {
            //Analyze GitHub Actions data
            LOG.info("Beginning GitHub Actions analysis of the repository with URL `"+ remoteUrl +"`...");
            this.attachGitHubActionsData(codebase, remoteUrl, oAuthToken);
            LOG.info("Finished GitHub Actions analysis for the repository with URL `"+ remoteUrl +"`.");
        }
        else {
            LOG.info("No CI selected.");
            return;
        }

        //Compute CI heat, recompute overall heat
        HeatCalculationUtility.assignHeatLevelsRelativeToAverage(codebase, HeatMetricOptionsExceptOverall.BUILD_FAILURE_SCORE);
        HeatCalculationUtility.assignHeatLevelsOverallOnly(codebase);
        //Persist codebase
        saveCodebase(codebase);
    }

    public void attachJenkinsData(Codebase codebase, String username, String apiKey, String jobUrl) throws IOException {
        //TODO: check if new Jenkins builds exist and skip re-calculation if all data is old

        JenkinsAnalyzer.attachJenkinsStackTraceActivityToCodebase(codebase, username, apiKey, jobUrl);

        saveCodebase(codebase);
    }

    public void attachGitHubActionsData(Codebase codebase, String remoteUrl, String oAuthToken) throws IOException {
        GithubActionsAnalyzer.attachBuildData(codebase, remoteUrl, oAuthToken);
        saveCodebase(codebase);
    }

    private void saveCodebase(Codebase codebase)
    {
        assert codebase.getGitHubUrl() != null;

        LOG.info("Saving FileObjects to database...");
        for (FileObject fileObject : codebase.getActiveFileObjects()) {
            fileObjectRepository.save(fileObject);
        }
        //Removed for now - we don't need to store commits.
//        LOG.info("Saving Commits to database...");
//        for (Commit commit : codebase.getActiveCommits()) {
//            commitRepository.save(commit);
//        }
        LOG.info("Saving Codebase to database...");
        codebaseRepository.save(codebase);
        LOG.info("All codebase data successfully saved to database.");
    }
}
