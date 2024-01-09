package com.insightservice.springboot.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.insightservice.springboot.payload.SettingsPayload;
import com.insightservice.springboot.service.FileSimilarityService;
import com.insightservice.springboot.utility.commit_history.JGitHelper;

import static com.insightservice.springboot.Constants.LOG;
import static com.insightservice.springboot.Constants.REPO_STORAGE_DIR;

@RestController
@RequestMapping("/api/fileSimilarity")
public class FileSimilarity {

    @Autowired
    FileSimilarityService fileSimilarityService;

    @PostMapping
    public ResponseEntity<?> fileSimilarity(@RequestBody SettingsPayload settingsPayload,
            BindingResult result) {

        try {
            String directoryName = JGitHelper
                    .getRepositoryNameFromUrl(settingsPayload.getGithubUrl() + settingsPayload.getBranchName());
            String repoPath = REPO_STORAGE_DIR + "/" + directoryName;
            return new ResponseEntity<>(fileSimilarityService.findSimilarity(repoPath), HttpStatus.OK);
        } catch (Exception e) {
            LOG.info("Mal informed URL");
            System.out.println(e);
        }

        return new ResponseEntity<>(null, HttpStatus.CONFLICT);

    }
}
