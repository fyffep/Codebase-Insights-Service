package com.insightservice.springboot.model.codebase;

import com.insightservice.springboot.observer.CodeBaseObservable;
import com.insightservice.springboot.Constants;
import com.insightservice.springboot.Constants.GroupingMode;
import com.insightservice.springboot.Constants.HeatMetricOptions;
import com.insightservice.springboot.observer.CodeBaseObserver;
import com.insightservice.springboot.utility.GroupFileObjectUtility;
import com.insightservice.springboot.utility.HeatCalculationUtility;
import com.insightservice.springboot.utility.RepositoryAnalyzer;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Codebase implements CodeBaseObservable {

    // region Vars
    private static TreeMap<String, TreeSet<FileObject>> packageBasedMapGroup;
    private static TreeMap<String, TreeSet<FileObject>> commitBasedMapGroup;
    private final List<CodeBaseObserver> observerList = new LinkedList<>();
    private final LinkedHashSet<String> branchNameList;
    private String activeBranch;
    private LinkedHashSet<Commit> activeCommits;
    private LinkedHashSet<FileObject> activeFileObjects;
    private String projectRootPath;
    private String latestCommitHash;
    private String targetCommit;
    private GroupingMode currentGroupingMode = GroupingMode.PACKAGES;
    private HeatMetricOptions currentHeatMetricOption = HeatMetricOptions.OVERALL; //TODO remove
    // endregion

    // region Constructor
    public Codebase() {
        activeBranch = "master";
        branchNameList = new LinkedHashSet<>();
        activeCommits = new LinkedHashSet<>();
        activeFileObjects = new LinkedHashSet<>();
        packageBasedMapGroup = new TreeMap<>();
        commitBasedMapGroup = new TreeMap<>();
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
    public String getActiveBranch() {
        return activeBranch;
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

    // region Controller Communication
    public void heatMapComponentSelected(String path) {
        FileObject selectedFile = getFileObjectFromFilename(RepositoryAnalyzer.getFilename(path));

        // Get commits associated with file
        ArrayList<Commit> associatedCommits = (ArrayList<Commit>) activeCommits.stream()
                .filter(commit -> commit.getFileSet().contains(selectedFile.getFilename()))
                .collect(Collectors.toList());

        notifyObserversOfRefreshFileCommitHistory(selectedFile, associatedCommits);
    }

    public void branchListRequested() {
        notifyObserversOfBranchList();
    }

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
//        RepositoryAnalyzer.attachCodebaseData(this); //TODO reconsider whether we should support branch change via cloning all branches at once
//
//        notifyObserversOfBranchChange(getSetOfFiles(), targetCommit, currentGroupingMode, currentHeatMetricOption);
//    }
    public void setActiveBranch(String activeBranch) {
        this.activeBranch = activeBranch;
    }

    public void newHeatMetricSelected(HeatMetricOptions newHeatMetricOption) {
        currentHeatMetricOption = newHeatMetricOption;

        notifyObserversOfRefreshHeatMap(getSetOfFiles(), targetCommit, currentGroupingMode, currentHeatMetricOption);
    }

    public void commitSelected(String commitHash) {
        Commit selectedCommit = getCommitFromCommitHash(commitHash);

        notifyObserversOfRefreshCommitDetails(selectedCommit);
    }

    public void changeHeatMapToCommit(String commitHash) {
        System.out.println("Update HeatMapComponents to this commitHash: " + commitHash);
        // TODO - Implement UI and backend logic.
    }


    public void heatMapGroupingChanged(@NotNull GroupingMode newGroupingMode) {
        currentGroupingMode = newGroupingMode;

        notifyObserversOfRefreshHeatMap(getSetOfFiles(), targetCommit, currentGroupingMode, currentHeatMetricOption);
    }
    // endregion

    //region Data packaging


    //FIXME @Abhishek
    /*public TreeMap<String, TreeSet<FileObject>> getSetOfFiles() {
        // Update views with data
        switch (currentGroupingMode) {
            case COMMITS:
                if (commitBasedMapGroup.isEmpty()) commitBasedMapGroup = groupDataByCommits();
                return commitBasedMapGroup;
            case PACKAGES:
            default:
                if (packageBasedMapGroup.isEmpty()) packageBasedMapGroup = groupDataByPackages();
                return packageBasedMapGroup;
        }
    }*/
    public TreeMap<String, TreeSet<FileObject>> getSetOfFiles() {
        // Update views with data
        TreeMap<String, TreeSet<FileObject>> setOfFiles;
        switch (currentGroupingMode) {
            case COMMITS:
                setOfFiles = groupDataByCommits();
                break;
            case PACKAGES:
            default:
                setOfFiles = groupDataByPackages();
                break;
        }
        return setOfFiles;
    }

    public TreeMap<String, TreeSet<FileObject>> groupDataByCommits() {
        System.out.println("groupDataByCommits called");

        // Calculate heat based on the selected metric
        HeatCalculationUtility.assignHeatLevels(this);

        //Group by commit
        return GroupFileObjectUtility.groupByCommit(this);
    }

    public TreeMap<String, TreeSet<FileObject>> groupDataByPackages() {
        System.out.println("groupDataByPackages called");

        // Calculate heat based on the selected metric
        HeatCalculationUtility.assignHeatLevels(this);

        //Group by package
        return GroupFileObjectUtility.groupByPackage(getProjectRootPath(), activeFileObjects);
    }

    //endregion

    // region Observable Methods
    @Override
    public void notifyObserversOfRefreshHeatMap(TreeMap<String, TreeSet<FileObject>> setOfFiles, String targetCommit, GroupingMode currentGroupingMode, HeatMetricOptions currentHeatMetricOption) {
        for (CodeBaseObserver observer : observerList) {
            observer.refreshHeatMap(setOfFiles, targetCommit, currentGroupingMode, currentHeatMetricOption);
        }
    }

    @Override
    public void notifyObserversOfBranchList() {
        for (CodeBaseObserver observer : observerList) {
            observer.branchListRequested(activeBranch, branchNameList.iterator());
        }
    }

    @Override
    public void notifyObserversOfBranchChange(TreeMap<String, TreeSet<FileObject>> setOfFiles, String targetCommit, GroupingMode currentGroupingMode, HeatMetricOptions currentHeatMetricOption) {
        for (CodeBaseObserver observer : observerList) {
            observer.newBranchSelected(setOfFiles, targetCommit, currentGroupingMode, currentHeatMetricOption);
        }
    }

    @Override
    public void notifyObserversOfRefreshFileCommitHistory(FileObject selectedFile, ArrayList<Commit> filesCommits) {
        for (CodeBaseObserver observer : observerList) {
            observer.fileSelected(selectedFile, filesCommits.iterator());
        }
    }

    @Override
    public void notifyObserversOfRefreshCommitDetails(Commit commit) {
        for (CodeBaseObserver observer : observerList) {
            observer.commitSelected(commit);
        }
    }

    @Override
    public void registerObserver(CodeBaseObserver observer) {
        observerList.add(observer);
    }

    @Override
    public void unregisterObserver(CodeBaseObserver observer) {
        observerList.remove(observer);
    }
    // endregion
}
