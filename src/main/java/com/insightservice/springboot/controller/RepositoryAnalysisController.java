package com.insightservice.springboot.controller;

import com.insightservice.springboot.model.codebase.*;
import com.insightservice.springboot.model.file_tree.RepoPackage;
import com.insightservice.springboot.payload.SettingsPayload;
import com.insightservice.springboot.service.RepositoryAnalysisService;
import com.insightservice.springboot.utility.DashboardCalculationUtility;
import com.insightservice.springboot.utility.FileTreeCreator;
import com.insightservice.springboot.utility.GroupFileObjectUtility;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

import static com.insightservice.springboot.Constants.LOG;
import static com.insightservice.springboot.Constants.USE_DEFAULT_BRANCH;

@RestController
@RequestMapping("/api/analyze")
public class RepositoryAnalysisController
{
    @Autowired
    private RepositoryAnalysisService repositoryAnalysisService;

    @GetMapping("/restcheck")
    public String test() {
        return "Rest check is working";
    }

    //When user updates settings, redo and store analysis
    @PostMapping("/initiate")
    public ResponseEntity<?> initiateAnalysis(@RequestBody SettingsPayload settingsPayload, BindingResult result) throws GitAPIException, IOException {
        String remoteUrl = settingsPayload.getGithubUrl();
        String oauthToken = settingsPayload.getGithubOAuthToken(); //TODO this is unused, so use it for JGit & store it on the user class

        LOG.info("Beginning analysis of the repository with URL `"+ remoteUrl +"`...");
        //Analyze Codebase
        Codebase codebase = repositoryAnalysisService.getOrCreateCodebase(remoteUrl, settingsPayload.getBranchName());
        //3rd-party CI tool analysis for build failures
        repositoryAnalysisService.runCiAnalysis(codebase, settingsPayload);

        codebase.setCommitBasedMapGroup(GroupFileObjectUtility.groupByCommit(codebase));

        return new ResponseEntity<Codebase>(codebase, HttpStatus.OK);
    }


    @PostMapping("/dashboard")
    public ResponseEntity<?> computeDashboardData(@RequestBody SettingsPayload urlPayload, BindingResult result) throws GitAPIException, IOException
    {
        String remoteUrl = urlPayload.getGithubUrl();

        //Retrieve codebase
        Codebase codebase = repositoryAnalysisService.getOrCreateCodebase(remoteUrl, USE_DEFAULT_BRANCH);

        //Get dashboard data
        DashboardModel dashboardModel = DashboardCalculationUtility.assignDashboardData(codebase);

        return new ResponseEntity<DashboardModel>(dashboardModel, HttpStatus.OK);
    }

    /**
     * Returns a tree structure of files for a Codebase with a RepoPackage as its root.
     */
    @PostMapping("/group-by-package")
    public ResponseEntity<?> performCodebaseAnalysisByPackage(@RequestBody SettingsPayload urlPayload, BindingResult result) throws GitAPIException, IOException
    {
        String remoteUrl = urlPayload.getGithubUrl();

        //Retrieve codebase
        Codebase codebase = repositoryAnalysisService.getOrCreateCodebase(remoteUrl, urlPayload.getBranchName());

        //Format the files present on the latest commit into a tree structure
        RepoPackage fileTree = FileTreeCreator.createFileTree(
                codebase.getActiveFileObjectsExcludeDeletedFiles(codebase.getLatestCommitHash()));

        return new ResponseEntity<>(fileTree, HttpStatus.OK);
    }
}
