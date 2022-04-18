package com.insightservice.springboot.controller;

import com.insightservice.springboot.model.codebase.*;
import com.insightservice.springboot.model.file_tree.RepoPackage;
import com.insightservice.springboot.payload.SettingsPayload;
import com.insightservice.springboot.service.RepositoryAnalysisService;
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

        LOG.info("Beginning analysis of the repository with URL `"+ remoteUrl +"`...");
        //Analyze Codebase
        Codebase codebase = repositoryAnalysisService.getOrCreateCodebase(settingsPayload);

        codebase.setCommitBasedMapGroup(GroupFileObjectUtility.groupByCommit(codebase));

        return new ResponseEntity<Codebase>(codebase, HttpStatus.OK);
    }

    /**
     * Returns a tree structure of files for a Codebase with a RepoPackage as its root.
     */
    @PostMapping("/group-by-package")
    public ResponseEntity<?> performCodebaseAnalysisByPackage(@RequestBody SettingsPayload settingsPayload, BindingResult result) throws GitAPIException, IOException
    {
        //Retrieve codebase
        Codebase codebase = repositoryAnalysisService.getOrCreateCodebase(settingsPayload);

        //Format the files present on the latest commit into a tree structure
        RepoPackage fileTree = FileTreeCreator.createFileTree(
                codebase.getActiveFileObjectsExcludeDeletedFiles(codebase.getLatestCommitHash()));

        return new ResponseEntity<>(fileTree, HttpStatus.OK);
    }
}
