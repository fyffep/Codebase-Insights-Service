package com.insightservice.springboot.controller;

import com.insightservice.springboot.model.codebase.Codebase;
import com.insightservice.springboot.model.codebase.FileObject;
import com.insightservice.springboot.payload.SettingsPayload;
import com.insightservice.springboot.service.RepositoryAnalysisService;
import com.insightservice.springboot.utility.GroupFileObjectUtility;
import com.insightservice.springboot.utility.RepositoryAnalyzer;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashMap;
import java.util.Set;
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
    public ResponseEntity<?> performCodebaseAnalysisByCommitContiguity(@RequestBody SettingsPayload settingsPayload, BindingResult result) throws GitAPIException, IOException
    {
        String remoteUrl = settingsPayload.getGithubUrl();

        //Retrieve Codebase
        Codebase codebase = repositoryAnalysisService.getOrCreateCodebase(remoteUrl, USE_DEFAULT_BRANCH);

        TreeMap<String, TreeSet<FileObject>> commitContiguityMap = codebase.getCommitBasedMapGroup();
        if (commitContiguityMap.isEmpty()) commitContiguityMap = GroupFileObjectUtility.groupByCommit(codebase);

        codebase.setCommitBasedMapGroup(commitContiguityMap);
        return new ResponseEntity<>(commitContiguityMap, HttpStatus.OK);
    }


    /**
     * UNUSED
     * Returns a tree structure of files for a Codebase with a RepoPackage as its root.
     */
    @PostMapping("/files-known")
    public ResponseEntity<?> getLinesAndFilePathsKnownPerAuthor(@RequestBody SettingsPayload settingsPayload, BindingResult result) throws GitAPIException, IOException
    {
        String remoteUrl = settingsPayload.getGithubUrl();
        RepositoryAnalyzer repositoryAnalyzer = new RepositoryAnalyzer(remoteUrl);

        return new ResponseEntity<HashMap<String, Pair<Integer, Set<String>>>>(repositoryAnalyzer.getKnowledge(), HttpStatus.OK);
    }

    /**
     * Returns a tree structure of files for a Codebase with a RepoPackage as its root.
     */
    @PostMapping("/graph")
    public ResponseEntity<?> getKnowledgeGraph(@RequestBody SettingsPayload settingsPayload, BindingResult result) throws GitAPIException, IOException
    {
        String remoteUrl = settingsPayload.getGithubUrl();
        RepositoryAnalyzer repositoryAnalyzer = new RepositoryAnalyzer(remoteUrl);

        return new ResponseEntity<>(repositoryAnalyzer.getKnowledgeGraph(), HttpStatus.OK);
    }
}
