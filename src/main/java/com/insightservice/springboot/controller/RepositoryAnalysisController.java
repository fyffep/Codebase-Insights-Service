package com.insightservice.springboot.controller;

import com.insightservice.springboot.model.codebase.Codebase;
import com.insightservice.springboot.model.codebase.DashboardModel;
import com.insightservice.springboot.payload.UrlPayload;
import com.insightservice.springboot.service.RepositoryAnalysisService;
import com.insightservice.springboot.utility.DashboardCalculationUtility;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

import static com.insightservice.springboot.Constants.LOG;

@RestController
@RequestMapping("/api/analyze")
public class RepositoryAnalysisController
{
    @Autowired
    private RepositoryAnalysisService repositoryAnalysisService;

    @PostMapping("/codebase")
    public ResponseEntity<?> cloneMyRepository(@RequestBody UrlPayload urlPayload, BindingResult result) throws GitAPIException, IOException
    {
        String remoteUrl = urlPayload.getGithubUrl();
        repositoryAnalysisService.cloneRemoteRepository(remoteUrl);

        LOG.info("Beginning analysis of the repository with URL `"+ remoteUrl +"`...");
        //Analyze Codebase
        Codebase codebase = RepositoryAnalysisService.extractData(remoteUrl);
        //DashboardCalculationUtility.assignDashboardData(codebase);

        return new ResponseEntity<Codebase>(codebase, HttpStatus.OK);
    }

    @PostMapping("/dashboard")
    public ResponseEntity<?> computeDashboardData(@RequestBody UrlPayload urlPayload, BindingResult result) throws GitAPIException, IOException
    {
        String remoteUrl = urlPayload.getGithubUrl();
        repositoryAnalysisService.cloneRemoteRepository(remoteUrl);

        LOG.info("Beginning analysis of the repository with URL `"+ remoteUrl +"`...");
        //Analyze Codebase
        Codebase codebase = RepositoryAnalysisService.extractData(remoteUrl);

        //TODO we'd want to write the codebase to the database here so that it can be retrieved later.

        //Get dashboard data
        DashboardModel dashboardModel = DashboardCalculationUtility.assignDashboardData(codebase);

        return new ResponseEntity<DashboardModel>(dashboardModel, HttpStatus.OK);
    }
}
