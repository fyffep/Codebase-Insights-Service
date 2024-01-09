package com.insightservice.springboot.controller;

import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.insightservice.springboot.model.codebase.Codebase;
import com.insightservice.springboot.model.fileComparisionBranch.SettingPayloadsWFilename;
import com.insightservice.springboot.payload.SettingsPayload;
import com.insightservice.springboot.service.FileHealthService;
import com.insightservice.springboot.service.RepositoryAnalysisService;
import com.insightservice.springboot.service.RetrieveAllBranchService;

import static com.insightservice.springboot.Constants.LOG;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/* Compares health of file across branch */
@RestController
@RequestMapping("/api/fileHealthAcrossBranch")
public class FileComparisionBranch {
    @Autowired
    FileHealthService fileHealthService;
    @Autowired
    RepositoryAnalysisService repositoryAnalysisService;
    @Autowired
    RetrieveAllBranchService retrieveAllBranchService;

    HashMap<String, Codebase> codebases = new HashMap<>();

    @PostMapping("/files")
    public ResponseEntity<?> mergeAndCompare(@RequestBody SettingPayloadsWFilename settingPayloadsWFilename,
            BindingResult result) {
        // can use for unit test
        Codebase codebase = repositoryAnalysisService.getCodebaseById(
                settingPayloadsWFilename.gSettingsPayload().getBranchName(),
                settingPayloadsWFilename.gSettingsPayload().getGithubUrl());

        if (codebase == null) {
            LOG.info("Cannot find or update repository; Make sure you have run analysis for this ...");
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        }

        return new ResponseEntity<>(fileHealthService.getAllFiles(codebase),
                HttpStatus.OK);
    }

    @PostMapping("/fileHealth")
    public ResponseEntity<?> compareFileHealthAcrossBranch(
            @RequestBody SettingPayloadsWFilename settingPayloadsWFilename, BindingResult result) {

        Codebase codebase = repositoryAnalysisService.getCodebaseById(
                settingPayloadsWFilename.gSettingsPayload().getBranchName(),
                settingPayloadsWFilename.gSettingsPayload().getGithubUrl());

        HashSet<String> branches = retrieveAllBranchService.getAllBranch(codebase);
        SettingsPayload settingsPayload = settingPayloadsWFilename.gSettingsPayload();

        if (branches.size() == 0) {
            LOG.info("Cannot retrieve all branches.. ");
            return new ResponseEntity<>(null, HttpStatus.OK);
        }

        if (this.codebases.size() == 0) {
            for (String branch : branches) {
                LOG.info("Retrieving branchname " + branch);
                Codebase c = repositoryAnalysisService.getCodebaseById(branch,
                        settingsPayload.getGithubUrl());
                this.codebases.put(branch, c);
            }
        }

        LOG.info("Size of codebase map " + this.codebases.size());

        return new ResponseEntity<>(
                fileHealthService.compareFileHealthAcrossBranch(settingPayloadsWFilename.gFilename(),
                        this.codebases),
                HttpStatus.OK);

    }

}
