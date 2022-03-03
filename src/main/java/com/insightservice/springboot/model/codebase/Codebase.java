package com.insightservice.springboot.model.codebase;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.insightservice.springboot.Constants;
import com.insightservice.springboot.Constants.GroupingMode;
import com.insightservice.springboot.utility.RepositoryAnalyzer;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.DBRef;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

public class Codebase
{
    // region Vars
    @Id
    private String gitHubUrl;
    @Transient
    private final LinkedHashSet<String> branchNameList;
    private String activeBranch;
    @JsonIgnore
    @Transient //lazy loaded
    private LinkedHashSet<Commit> activeCommits;
    @DBRef //eagerly loaded
    private LinkedHashSet<FileObject> activeFileObjects;
    private String projectRootPath;
    private String latestCommitHash;
    private String targetCommit;
    private GroupingMode currentGroupingMode = GroupingMode.PACKAGES;
    // endregion

    // region Constructor
    public Codebase() {
        activeBranch = "master";
        branchNameList = new LinkedHashSet<>();
        activeCommits = new LinkedHashSet<>();
        activeFileObjects = new LinkedHashSet<>();
    }
    // endregion

    public void selectDefaultBranch() throws IOException {
        String branch = "";
        for (String defaultBranch : Constants.DEFAULT_BRANCHES) {
            if (branchNameList.contains(defaultBranch.toLowerCase())) {
                branch = defaultBranch;
                break;
            }
        }

        // Means no default branches are in branchNameList
        if (branch.isEmpty()) {
            // So, just grab the first branch
            Optional<String> optional = branchNameList.stream().findFirst();
            if (optional.isPresent())
                branch = optional.get();
            else {
                //Potentially, we could instead default to "No branches found" to keep the plugin window empty.
                //For now, we'll throw an exception.
                throw new IOException("Could not find any branches in the Git repository to be analyzed"); //FIXME determine how to handle the situation where the local repository cannot be found
            }
        }

        activeBranch = branch;
    }

    // region Getters/Setters
    public String getGitHubUrl() {
        return gitHubUrl;
    }

    public void setGitHubUrl(String gitHubUrl) {
        this.gitHubUrl = gitHubUrl;
    }

    public String getActiveBranch() {
        return activeBranch;
    }

    public void setActiveBranch(String activeBranch) {
        this.activeBranch = activeBranch;
    }

    public LinkedHashSet<String> getBranchNameList() {
        return branchNameList;
    }

    public LinkedHashSet<Commit> getActiveCommits() {
        return activeCommits;
    }

    public HashSet<FileObject> getActiveFileObjects() {
        return activeFileObjects;
    }

    /**
     * Returns a copy of Codebase's file set.
     * It represents all files present at the target commit, excluding the files deleted by people.
     */
    public HashSet<FileObject> getActiveFileObjectsExcludeDeletedFiles(String commitHash) {
        HashSet<FileObject> fileSetCopy = new HashSet<>();
        for (FileObject fileObject : activeFileObjects)
        {
            HeatObject heatObject = fileObject.getHeatObjectAtCommit(commitHash);
            if (heatObject != null)
                fileSetCopy.add(fileObject);
            //else, if there is no HeatObject for the file at the target commit, the file was deleted
        }

        return fileSetCopy;
    }

    public String getProjectRootPath() {
        return projectRootPath;
    }

    public void setProjectRootPath(String projectRootPath) {
        this.projectRootPath = projectRootPath;
    }

    public String getLatestCommitHash() {
        return latestCommitHash;
    }

    public void setLatestCommitHash(String latestCommitHash) {
        this.targetCommit = latestCommitHash;
        this.latestCommitHash = latestCommitHash;
    }


    // Should only be used when building model data
    public FileObject createOrGetFileObjectFromPath(String path) {
        FileObject selectedFile = activeFileObjects.stream()
                .filter(file -> file.getFilename().equals(RepositoryAnalyzer.getFilename(path))).findAny().orElse(null);

        // Failed to find file associated with param id
        if (selectedFile == null) {
            // Create and return new FileObject
            selectedFile = new FileObject(Paths.get(path));
            activeFileObjects.add(selectedFile);
        }
        return selectedFile;
    }

    // If not building model data we want a null return
    // This ensures we know something went wrong. Which means we are looking for a filename that doesn't exist in our model's data
    public FileObject getFileObjectFromFilename(String filename) {
        return activeFileObjects.stream()
                .filter(file -> file.getFilename().equals(filename)).findAny().orElse(null);
    }

    public Commit getCommitFromCommitHash(String commitHash) {
        Commit selectedCommit = activeCommits.stream()
                .filter(commit -> commit.getHash().equals(commitHash)).findAny().orElse(null);

        // Failed to find file associated with param id
        if (selectedCommit == null) {
            throw new NullPointerException(String.format("Failed to find the proper commit associated with the selected commit in the TableView. Hash = %s", commitHash));
        }

        return selectedCommit;
    }
    // endregion


//    public void newBranchSelected(String branchName) {
//        // Branch doesn't exist - or we don't know about it some how...
//        if (!branchNameList.contains(branchName) && !branchName.isEmpty()) {
//            throw new UnsupportedOperationException(String.format("Branch %s was selected but is not present in branchNameList.", branchName));
//        }
//
//        this.activeBranch = branchName;
//
//        // Dump old data and create new sets
//        activeCommits.clear();
//        activeCommits = new LinkedHashSet<>();
//        activeFileObjects.clear();
//        activeFileObjects = new LinkedHashSet<>();
//        packageBasedMapGroup = new TreeMap<>();
//        packageBasedMapGroup.clear();
//        commitBasedMapGroup = new TreeMap<>();
//        commitBasedMapGroup.clear();
//        latestCommitHash = "";
//
//        RepositoryAnalyzer.attachCodebaseData(this);
//
//        notifyObserversOfBranchChange(getSetOfFiles(), targetCommit, currentGroupingMode, currentHeatMetricOption);
//    }

    //endregion
}
