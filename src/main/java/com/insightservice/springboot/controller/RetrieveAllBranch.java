package com.insightservice.springboot.controller;
/* This file compares health between two branches */

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.insightservice.springboot.model.codebase.Codebase;
import com.insightservice.springboot.payload.SettingsPayload;
import com.insightservice.springboot.service.RepositoryAnalysisService;
import com.insightservice.springboot.service.RetrieveAllBranchService;
import static com.insightservice.springboot.Constants.LOG;

@RestController
@RequestMapping("/api/branches")
public class RetrieveAllBranch {

    @Autowired
    RetrieveAllBranchService retrieveAllBranchService;
    @Autowired
    RepositoryAnalysisService repositoryAnalysisService;

    @PostMapping
    public ResponseEntity<?> mergeAndCompare(@RequestBody SettingsPayload settingsPayload, BindingResult result) {
        Codebase codebase = repositoryAnalysisService.getCodebaseById(
                settingsPayload.getBranchName(),
                settingsPayload.getGithubUrl());
                

        if (codebase == null) {
            LOG.info("Cannot find or update repository; Make sure you have run analysis for this ...");
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }

        return new ResponseEntity<>(retrieveAllBranchService.getAllBranch(codebase), HttpStatus.OK);

    }

}
