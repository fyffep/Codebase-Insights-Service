package com.insightservice.springboot.service;

import com.insightservice.springboot.model.codebase.Codebase;
import com.insightservice.springboot.model.codebase.Commit;
import com.insightservice.springboot.model.codebase.FileObject;
import com.insightservice.springboot.repository.CodebaseRepository;
import com.insightservice.springboot.repository.CommitRepository;
import com.insightservice.springboot.repository.FileObjectRepository;
import com.insightservice.springboot.utility.HeatCalculationUtility;
import com.insightservice.springboot.utility.RepositoryAnalyzer;
import com.insightservice.springboot.utility.commit_history.JGitHelper;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

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
            codebase = extractDataToCodebase(remoteUrl, branchName);
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
            LOG.info("Saving Codebase to database...");
            codebaseRepository.save(codebase);
            LOG.info("Saving FileObjects to database...");
            for (FileObject fileObject : codebase.getActiveFileObjects()) {
                fileObject.setPathForDatabase(fileObject.getPath().toString());
                fileObjectRepository.save(fileObject);
            }
            for (Commit commit : codebase.getActiveCommits()) {
                commitRepository.save(commit);
            }
            LOG.info("Saving Commits to database...");
            LOG.info("All codebase data successfully saved to database.");

            return codebase;
        }
        finally
        {
            //Close the .git files
            if (repositoryAnalyzer != null)
                repositoryAnalyzer.cleanup();
        }
    }
}
