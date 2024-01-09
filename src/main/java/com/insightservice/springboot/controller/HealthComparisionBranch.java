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

import com.insightservice.springboot.model.branchHealthComparision.SettingPayloadWBranches;
import com.insightservice.springboot.model.branchHealthComparision.TwoBranches;
import com.insightservice.springboot.model.codebase.Codebase;
import com.insightservice.springboot.model.codebase.CodebaseId;
import com.insightservice.springboot.payload.SettingsPayload;
import com.insightservice.springboot.repository.CodebaseRepository;
import com.insightservice.springboot.service.HealthComparisionService;
import com.insightservice.springboot.service.RepositoryAnalysisService;
import com.insightservice.springboot.utility.commit_history.JGitHelper;

import static com.insightservice.springboot.Constants.LOG;

@RestController
@RequestMapping("/api/mergeAndCompare")
public class HealthComparisionBranch {

    @Autowired
    HealthComparisionService healthComparisionService;
    @Autowired
    RepositoryAnalysisService repositoryAnalysisService;
    @Autowired
    CodebaseRepository codebaseRepository;

    private boolean codebaseExists(String branch, String github) {
        CodebaseId id = new CodebaseId();
        id.setBranchName(branch);
        id.setGithubUrl(github);

        return codebaseRepository.existsById(id);

    }

    @PostMapping
    public ResponseEntity<?> mergeAndCompare(@RequestBody SettingPayloadWBranches settingPayloadWBranches,
            BindingResult result) {

        SettingsPayload settingsPayload = settingPayloadWBranches.gSettingsPayload();
        TwoBranches twoBranches = settingPayloadWBranches.gBranches();

        try {
            JGitHelper.cloneOrUpdateRepository(settingsPayload.getGithubUrl(), settingsPayload.getBranchName(),
                    settingsPayload.getGithubOAuthToken());
        } catch (Exception e) {
            LOG.info("Cannot clone or update repository ...");
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }

        if (!codebaseExists(twoBranches.parentBranch, settingsPayload.getGithubUrl())) {
            LOG.info("Repo for branch " + twoBranches.parentBranch
                    + " does not exist, have you run analysis on this branch?");
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }

        if (!codebaseExists(twoBranches.childBranch, settingsPayload.getGithubUrl())) {
            LOG.info("Repo for branch " + twoBranches.childBranch
                    + " does not exist, have you run analysis on this branch?");
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }

        Codebase parentCodebase = repositoryAnalysisService.getCodebaseById(twoBranches.parentBranch,
                settingsPayload.getGithubUrl());
        Codebase childCodebase = repositoryAnalysisService.getCodebaseById(twoBranches.childBranch,
                settingsPayload.getGithubUrl());

        if (parentCodebase == null || childCodebase == null) {
            LOG.info("Please run analysis of both branch before comparing");
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }

        return new ResponseEntity<>(healthComparisionService.getHealthDifference(twoBranches,
                settingsPayload.getGithubUrl(), parentCodebase, childCodebase), HttpStatus.OK);
    }

}
