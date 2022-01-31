package com.insightservice.springboot.observer;


import com.insightservice.springboot.Constants;
import com.insightservice.springboot.model.codebase.Commit;
import com.insightservice.springboot.model.codebase.FileObject;

import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;

public interface CodeBaseObserver {
    void refreshHeatMap(TreeMap<String, TreeSet<FileObject>> setOfFiles, String targetCommit, Constants.GroupingMode currentGroupingMode, Constants.HeatMetricOptions currentHeatMetricOption);

    // notifyObserversOfBranchList
    void branchListRequested(String activeBranch, Iterator<String> branchList);

    // notifyObserversOfBranchChange
    void newBranchSelected(TreeMap<String, TreeSet<FileObject>> setOfFiles, String targetCommit, Constants.GroupingMode currentGroupingMode, Constants.HeatMetricOptions currentHeatMetricOption);

    // notifyObserversOfRefreshFileCommitHistory
    void fileSelected(FileObject selectedFile, Iterator<Commit> filesCommits);

    // notifyObserversOfRefreshCommitDetails
    void commitSelected(Commit commit);
}
