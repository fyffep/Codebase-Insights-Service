package com.insightservice.springboot.controller;

import com.insightservice.springboot.model.codebase.Codebase;
import com.insightservice.springboot.model.codebase.DashboardModel;
import com.insightservice.springboot.model.codebase.FileObject;
import com.insightservice.springboot.payload.UrlPayload;
import com.insightservice.springboot.service.RepositoryAnalysisService;
import com.insightservice.springboot.utility.DashboardCalculationUtility;
import com.insightservice.springboot.utility.GroupFileObjectUtility;
import com.insightservice.springboot.utility.commit_history.JGitHelper;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;
import java.util.TreeSet;

import static com.insightservice.springboot.Constants.LOG;
import static com.insightservice.springboot.Constants.USE_DEFAULT_BRANCH;

@RestController
@RequestMapping("/api/analyze")
public class RepositoryAnalysisController
{
    @Autowired
    private RepositoryAnalysisService repositoryAnalysisService;

    @PostMapping("/codebase")
    public ResponseEntity<?> analyzeEntireCodebase(@RequestBody UrlPayload urlPayload, BindingResult result) throws GitAPIException, IOException
    {
        String remoteUrl = urlPayload.getGithubUrl();

        LOG.info("Beginning analysis of the repository with URL `"+ remoteUrl +"`...");
        //Analyze Codebase
        Codebase codebase = repositoryAnalysisService.extractDataToCodebase(remoteUrl, USE_DEFAULT_BRANCH);

        return new ResponseEntity<Codebase>(codebase, HttpStatus.OK);
    }

    @PostMapping("/dashboard")
    public ResponseEntity<?> computeDashboardData(@RequestBody UrlPayload urlPayload, BindingResult result) throws GitAPIException, IOException
    {
        String remoteUrl = urlPayload.getGithubUrl();

        LOG.info("Beginning analysis of the repository with URL `"+ remoteUrl +"`...");
        //Analyze Codebase
        Codebase codebase = repositoryAnalysisService.extractDataToCodebase(remoteUrl, USE_DEFAULT_BRANCH);

        //TODO we'd want to write the codebase to the database here so that it can be retrieved later.

        //Get dashboard data
        DashboardModel dashboardModel = DashboardCalculationUtility.assignDashboardData(codebase);

        return new ResponseEntity<DashboardModel>(dashboardModel, HttpStatus.OK);
    }

    @PostMapping("/group-by-package")
    public ResponseEntity<?> performCodebaseAnalysisByPackage(@RequestBody UrlPayload urlPayload, BindingResult result) throws GitAPIException, IOException
    {
        String remoteUrl = urlPayload.getGithubUrl();

        LOG.info("Beginning analysis of the repository with URL `"+ remoteUrl +"`...");
        //Analyze Codebase
        Codebase codebase = repositoryAnalysisService.extractDataToCodebase(remoteUrl, USE_DEFAULT_BRANCH);

        String pathToRepos = JGitHelper.getPathOfLocalRepository(remoteUrl).getPath();
        codebase.setProjectRootPath(pathToRepos);

        Map<String, TreeSet<FileObject>> packageToFileMap = GroupFileObjectUtility.groupByPackage(codebase.getProjectRootPath(), codebase.getActiveFileObjects()); //method being tested

        return new ResponseEntity<Map<String, TreeSet<FileObject>>>(packageToFileMap, HttpStatus.OK);
    }
}
