package com.insightservice.springboot.controller;

import com.insightservice.springboot.model.codebase.*;
import com.insightservice.springboot.model.file_tree.RepoPackage;
import com.insightservice.springboot.payload.UrlPayload;
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

import java.io.File;
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

    @PostMapping("/initiate")
    public ResponseEntity<?> initiateAnalysis(@RequestBody UrlPayload urlPayload, BindingResult result) throws GitAPIException, IOException
    {
        //TODO Jenkins url & apiKey need to be sent as well
        String remoteUrl = urlPayload.getGithubUrl();

        String branchName = "intentional-bugs"; //TEMP for Jenkins

        LOG.info("Beginning analysis of the repository with URL `"+ remoteUrl +"`...");
        //Analyze Codebase
        Codebase codebase = repositoryAnalysisService.getOrCreateCodebase(remoteUrl, branchName);

        //Analyze Jenkins data
        LOG.info("Beginning Jenkins analysis of the repository with URL `"+ remoteUrl +"`...");
        repositoryAnalysisService.attachJenkinsData(codebase);
        codebase.setCommitBasedMapGroup(GroupFileObjectUtility.groupByCommit(codebase));

        return new ResponseEntity<Codebase>(codebase, HttpStatus.OK);
    }


    @PostMapping("/dashboard")
    public ResponseEntity<?> computeDashboardData(@RequestBody UrlPayload urlPayload, BindingResult result) throws GitAPIException, IOException
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
    public ResponseEntity<?> performCodebaseAnalysisByPackage(@RequestBody UrlPayload urlPayload, BindingResult result) throws GitAPIException, IOException
    {
        String remoteUrl = urlPayload.getGithubUrl();

        //Retrieve codebase
        Codebase codebase = repositoryAnalysisService.getOrCreateCodebase(remoteUrl, USE_DEFAULT_BRANCH);

        //Format the files present on the latest commit into a tree structure
        RepoPackage fileTree = FileTreeCreator.createFileTree(
                codebase.getActiveFileObjectsExcludeDeletedFiles(codebase.getLatestCommitHash()));

        return new ResponseEntity<>(fileTree, HttpStatus.OK);
    }
}
