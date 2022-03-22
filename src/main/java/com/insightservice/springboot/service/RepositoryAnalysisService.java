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
import com.insightservice.springboot.utility.JenkinsAnalyzer;
import com.insightservice.springboot.utility.RepositoryAnalyzer;
import com.insightservice.springboot.utility.commit_history.JGitHelper;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static com.insightservice.springboot.Constants.LOG;
import static com.insightservice.springboot.Constants.USE_DEFAULT_BRANCH;

@Service
public class RepositoryAnalysisService
{
    @Autowired
    CodebaseRepository codebaseRepository;
    @Autowired
    FileObjectRepository fileObjectRepository;
    @Autowired
    CommitRepository commitRepository;

    public void cloneRemoteRepository(String remoteUrl, String branchName) throws GitAPIException, IOException
    {
        JGitHelper.cloneRepository(remoteUrl, branchName);
    }


    public Codebase getOrCreateCodebase(String remoteUrl, String branchName) throws GitAPIException, IOException
    {
        Codebase codebase = codebaseRepository.findById(remoteUrl).orElse(null);
        //If codebase is new OR
        //if branch changed OR
        //if codebase is outdated due to a new commit
        if (codebase == null ||
                (!codebase.getActiveBranch().equals(branchName) && !branchName.equals(USE_DEFAULT_BRANCH)) ||
                !JGitHelper.checkIfLatestCommitIsUpToDate(codebase))
        {
            LOG.info("Beginning new Codebase analysis because the repo is new or updated...");
            codebase = extractDataToCodebase(remoteUrl, branchName);
        }
        //Else, up-to-date codebase data exists
        else {
            LOG.info("Returning old Codebase data because the repo is up-to-date...");
        }

        return codebase;
    }

    /**
     * Clones a GitHub repos into local memory, then analyzes it.
     * All data is placed into a Codebase.
     * Finally, the local repos is deleted from the file system.
     * @param remoteUrl the URL to the home page of a user's GitHub repository
     * @return the Codebase containing all heat and file data.
     */
    private Codebase extractDataToCodebase(String remoteUrl, String branchName) throws GitAPIException, IOException
    {
        //Obtain file metrics by analyzing the code base
        RepositoryAnalyzer repositoryAnalyzer = null;
        try
        {
            this.cloneRemoteRepository(remoteUrl, branchName);
            Codebase codebase = new Codebase();

            //Calculate heat metrics for every commit
            repositoryAnalyzer = new RepositoryAnalyzer(remoteUrl);
            RepositoryAnalyzer.attachBranchNameList(codebase);
            codebase.selectDefaultBranch();
            RepositoryAnalyzer.attachCodebaseData(codebase);

            HeatCalculationUtility.assignHeatLevels(codebase);

            //Now the Codebase contains all the data it needs
            LOG.info("Heat calculations complete. Number of files: " + codebase.getActiveFileObjects().size());

            //Persist the codebase
            codebase.setGitHubUrl(remoteUrl);
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

    public void runCiAnalysis(Codebase codebase, SettingsPayload settingsPayload) throws IOException
    {
        String remoteUrl = settingsPayload.getGithubUrl();
        String ciToolChosen = settingsPayload.getCiToolChosen();
        if (ciToolChosen.equals("Jenkins"))
        {
            //Analyze Jenkins data
            LOG.info("Beginning Jenkins analysis of the repository with URL `"+ remoteUrl +"`...");
            this.attachJenkinsData(codebase, settingsPayload.getCiUsername(), settingsPayload.getApiKey(), settingsPayload.getJobUrl());
            codebase.setCommitBasedMapGroup(GroupFileObjectUtility.groupByCommit(codebase));
        }
        else if (ciToolChosen.equals("GitHub Actions"))
        {
            //TODO
            LOG.info("TODO GitHub Actions analysis of the repository with URL `"+ remoteUrl +"`...");
        }
        else
        {
            LOG.info("Removing CI analysis for the repository with URL `"+ remoteUrl +"`...");
            //TODO remove CI credentials from the user
        }
    }

    public void attachJenkinsData(Codebase codebase, String username, String apiKey, String jobUrl) throws IOException
    {
        //TODO: check if new Jenkins builds exist and skip re-calculation if all data is old

        JenkinsAnalyzer.attachJenkinsStackTraceActivityToCodebase(codebase, username, apiKey, jobUrl);

        saveCodebase(codebase);
    }



    private void saveCodebase(Codebase codebase)
    {
        assert codebase.getGitHubUrl() != null;

        LOG.info("Saving FileObjects to database...");
        for (FileObject fileObject : codebase.getActiveFileObjects()) {
            fileObjectRepository.save(fileObject);
        }
        LOG.info("Saving Commits to database...");
        for (Commit commit : codebase.getActiveCommits()) {
            commitRepository.save(commit);
        }
        LOG.info("Saving Codebase to database...");
        codebaseRepository.save(codebase);
        LOG.info("All codebase data successfully saved to database.");
    }
}
