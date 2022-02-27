package com.insightservice.springboot.controller;

import com.insightservice.springboot.model.codebase.Codebase;
import com.insightservice.springboot.payload.UrlPayload;
import com.insightservice.springboot.service.RepositoryAnalysisService;
import com.insightservice.springboot.utility.JenkinsAnalyzer;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

import static com.insightservice.springboot.Constants.LOG;
import static com.insightservice.springboot.Constants.USE_DEFAULT_BRANCH;


@RestController
@RequestMapping("/api/jenkins")
public class JenkinsCodebaseController
{
    @Autowired
    private RepositoryAnalysisService repositoryAnalysisService;

    @PostMapping("/codebase")
    public ResponseEntity<?> analyzeEntireCodebase(@RequestBody UrlPayload urlPayload, BindingResult result) throws GitAPIException, IOException
    {
        String remoteUrl = urlPayload.getGithubUrl();

        LOG.info("Beginning general analysis of the repository with URL `"+ remoteUrl +"`...");
        //Analyze Codebase
        String branchName = "intentional-bugs"; //TEMP
        Codebase codebase = repositoryAnalysisService.getOrCreateCodebase(remoteUrl, branchName);

        LOG.info("Beginning Jenkins analysis of the repository with URL `"+ remoteUrl +"`...");
        JenkinsAnalyzer.attachJenkinsStackTraceActivityToCodebase(codebase); //TODO set up a Service for this

        return new ResponseEntity<Codebase>(codebase, HttpStatus.OK);
    }
}
