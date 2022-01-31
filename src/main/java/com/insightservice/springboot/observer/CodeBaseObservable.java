package com.insightservice.springboot.observer;

import com.insightservice.springboot.Constants;
import com.insightservice.springboot.Constants.GroupingMode;
import com.insightservice.springboot.Constants.HeatMetricOptions;
import com.insightservice.springboot.model.codebase.Commit;
import com.insightservice.springboot.model.codebase.FileObject;

import java.util.ArrayList;
import java.util.TreeMap;
import java.util.TreeSet;

public interface CodeBaseObservable {
    void notifyObserversOfRefreshHeatMap(TreeMap<String, TreeSet<FileObject>> setOfFiles, String targetCommit, GroupingMode currentGroupingMode, HeatMetricOptions currentHeatMetricOption);

    // branchListRequested
    void notifyObserversOfBranchList();

    // branchSelected
    void notifyObserversOfBranchChange(TreeMap<String, TreeSet<FileObject>> setOfFiles, String targetCommit, GroupingMode currentGroupingMode, HeatMetricOptions currentHeatMetricOption);

    // fileSelected
    void notifyObserversOfRefreshFileCommitHistory(FileObject selectedFile, ArrayList<Commit> filesCommits);

    // commitSelected
    void notifyObserversOfRefreshCommitDetails(Commit commit);

    void registerObserver(CodeBaseObserver observer);

    void unregisterObserver(CodeBaseObserver observer);
}
