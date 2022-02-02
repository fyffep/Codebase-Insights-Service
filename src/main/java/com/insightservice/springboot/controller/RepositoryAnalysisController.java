package com.insightservice.springboot.controller;

import com.insightservice.springboot.model.codebase.Codebase;
import com.insightservice.springboot.payload.UrlPayload;
import com.insightservice.springboot.service.RepositoryAnalysisService;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
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
        //DashboardCalculationUtility.assignDashboardData(); //TODO

        return new ResponseEntity<Codebase>(codebase, HttpStatus.OK);
    }

    //Unused. This version uses path var instead of UrlResquest.
    /*@PostMapping("/clone-repository/${remoteUrl}")
    public ResponseEntity<?> cloneMyRepository(@PathVariable remoteUrl) throws GitAPIException, IOException
    {
        repositoryAnalysisService.cloneMyRepository(remoteUrl);

        return new ResponseEntity<String>("Successfully cloned your repository.", HttpStatus.OK);
    }*/
}
