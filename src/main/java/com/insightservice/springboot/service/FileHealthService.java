package com.insightservice.springboot.service;

import java.util.HashMap;
import java.util.HashSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.insightservice.springboot.model.codebase.Codebase;
import com.insightservice.springboot.model.file_tree.RepoPackage;
import com.insightservice.springboot.payload.SettingsPayload;
import com.insightservice.springboot.utility.FileTreeCreator;
import static com.insightservice.springboot.Constants.LOG;

@Service
public class FileHealthService {
    @Autowired
    RepositoryAnalysisService repositoryAnalysisService;

    public RepoPackage getAllFiles(Codebase codebase) {

        return (RepoPackage) FileTreeCreator.createFileTree(
                codebase.getActiveFileObjectsExcludeDeletedFiles(codebase.getLatestCommitHash()));

    }

    public HashMap<String, Double> compareFileHealthAcrossBranch(String filename, HashMap<String, Codebase> codebases) {
        // get data in format {}
        HashMap<String, Double> result = new HashMap<>();
        codebases.forEach((branchName, codebase) -> {
            try {
                Double heatmap = codebase.getFileObjectFromFilename(filename).getLatestHeatObject().getOverallHeat();
                LOG.info(branchName, heatmap);
                result.put(branchName, heatmap);
            } catch (NullPointerException ex) {
                LOG.info(filename + " not found in branch " + branchName);
            }

        });

        return result;
    }

}
