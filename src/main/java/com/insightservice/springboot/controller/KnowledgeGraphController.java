package com.insightservice.springboot.controller;

import com.insightservice.springboot.model.codebase.Codebase;
import com.insightservice.springboot.model.codebase.FileObject;
import com.insightservice.springboot.payload.SettingsPayload;
import com.insightservice.springboot.service.RepositoryAnalysisService;
import com.insightservice.springboot.utility.GroupFileObjectUtility;
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
import java.util.TreeMap;
import java.util.TreeSet;

import static com.insightservice.springboot.Constants.USE_DEFAULT_BRANCH;

@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeGraphController
{
    @Autowired
    RepositoryAnalysisService repositoryAnalysisService;


    /**
     * Returns a tree structure of files for a Codebase with a RepoPackage as its root.
     */
    @PostMapping("/group-by-commit-contiguity")
    public ResponseEntity<?> performCodebaseAnalysisByCommitContiguity(@RequestBody SettingsPayload urlPayload, BindingResult result) throws GitAPIException, IOException
    {
        String remoteUrl = urlPayload.getGithubUrl();

        //Retrieve Codebase
        Codebase codebase = repositoryAnalysisService.getOrCreateCodebase(remoteUrl, USE_DEFAULT_BRANCH);

        TreeMap<String, TreeSet<FileObject>> commitContiguityMap = codebase.getCommitBasedMapGroup();
        if (commitContiguityMap.isEmpty()) commitContiguityMap = GroupFileObjectUtility.groupByCommit(codebase);

        codebase.setCommitBasedMapGroup(commitContiguityMap);
        return new ResponseEntity<>(commitContiguityMap, HttpStatus.OK);
    }
}
