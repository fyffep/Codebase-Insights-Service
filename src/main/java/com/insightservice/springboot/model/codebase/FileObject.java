package com.insightservice.springboot.model.codebase;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.insightservice.springboot.Constants;
import com.insightservice.springboot.model.file_tree.RepoTreeNode;
import com.insightservice.springboot.utility.RepositoryAnalyzer;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * FileObjectV2
 * - filename acts as ID
 * - Path path
 * - Map<CommitHash, HeatObject>;
 * - HeatObject has the metrics for this file per commit
 */
public class FileObject implements RepoTreeNode
{
    // region Variables
    @Id
    private Path path;
    private String filename;
    @JsonIgnore
    private LinkedHashMap<String, HeatObject> commitHashToHeatObjectMap;
    private int degreeOfCouplingHeat;
    private HeatObject latestHeatObject; //heat levels at the latest commit
    private Set<String> uniqueAuthors;
    private Set<String> uniqueAuthorEmails;
    // This would maintain the latest key commit hash added in the map to avoid any traversal again
    @Transient
    @JsonIgnore
    private String latestCommitInTreeWalk; // last time this file appeared in the TreeWalk
    @Transient
    @JsonIgnore
    private String latestCommitInDiffEntryList; // last time this file appeared in the DiffEntry
    // endregion

    // region Constructors
    public FileObject() {
        //Empty constructor
    }

    public FileObject(Path path) {
        this.path = path;
        this.filename = RepositoryAnalyzer.getFilename(this.path.toString());
        this.commitHashToHeatObjectMap = new LinkedHashMap<>();
        this.uniqueAuthors = new LinkedHashSet<>();
        this.uniqueAuthorEmails = new LinkedHashSet<>();
        this.latestCommitInTreeWalk = "";
        this.latestCommitInDiffEntryList = "";
        this.degreeOfCouplingHeat = 0;
    }
    // endregion


    public Path getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = Paths.get(path);
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Set<String> getUniqueAuthors() {
        return uniqueAuthors;
    }

    public Set<String> getUniqueAuthorEmails() {
        return uniqueAuthorEmails;
    }

    public String getLatestCommitInTreeWalk() {
        return latestCommitInTreeWalk;
    }

    public void setLatestCommitInTreeWalk(String latestCommitInTreeWalk) {
        this.latestCommitInTreeWalk = latestCommitInTreeWalk;
    }

    public String getLatestCommitInDiffEntryList() {
        return latestCommitInDiffEntryList;
    }

    public void setLatestCommitInDiffEntryList(String latestCommitInDiffEntryList) {
        this.latestCommitInDiffEntryList = latestCommitInDiffEntryList;
    }

    public int getDegreeOfCouplingHeat() {
        return degreeOfCouplingHeat;
    }

    /**
     * This method computes degree of external coupling heat based on degreeOfExternalCoupling
     * derived from group by commit contiguity logic.
     * If a file forms a group with 10 other files in the codebase which is also the total number of active files,
     * the file would have MAX HEAT.
     * @param degreeOfExternalCoupling value for the number of files forming a group with the current.
     * @param totalExternalFiles value for the total number of active files in the codebase except the current.
     */
    public void computeDegreeOfCouplingHeat(int degreeOfExternalCoupling, int totalExternalFiles) {
        this.degreeOfCouplingHeat = (int) ((degreeOfExternalCoupling * 1.0/totalExternalFiles) * Constants.HEAT_MAX);
    }


    // Find/return existing or create new HeatObject for commitHash
    public HeatObject createOrGetHeatObjectAtCommit(String commitHash) {
        HeatObject existingHeatObject = commitHashToHeatObjectMap.get(commitHash);

        if(existingHeatObject != null) {
            return existingHeatObject;
        } else {
            HeatObject newHeatObject = new HeatObject();
            commitHashToHeatObjectMap.put(commitHash, newHeatObject);
            return newHeatObject;
        }
    }

    // Return null if not found
    public HeatObject getHeatObjectAtCommit(String commitHash) {
        return commitHashToHeatObjectMap.get(commitHash);
    }

    public LinkedHashMap<String, HeatObject> getCommitHashToHeatObjectMap() {
        return commitHashToHeatObjectMap;
    }

    public void setHeatForCommit(String commitHash, HeatObject heat) {
        // commitHash already present - was this intentional?
        if (commitHashToHeatObjectMap.putIfAbsent(commitHash, heat) != null) {
            throw new UnsupportedOperationException(String.format("Commit hash %s is already present in %s's commitHashToHeatObjectMap.", commitHash, filename));
        }

        this.latestCommitInTreeWalk = commitHash;
    }

    public HeatObject getLatestHeatObject() {
        return latestHeatObject;
    }

    public void setLatestHeatObject(HeatObject latestHeatObject) {
        this.latestHeatObject = latestHeatObject;
    }

    public String getHeatMetricString(HeatObject heatObject, Constants.HeatMetricOptions heatMetricOption) {
        String text = "";
        switch (heatMetricOption) {
            case FILE_SIZE:
                text = String.format("%s: %d characters", Constants.FILE_SIZE_TEXT, heatObject.getFileSize());
                break;
            case NUM_OF_AUTHORS:
                text = String.format("%s: %d", Constants.NUMBER_OF_AUTHORS_TEXT, heatObject.getNumberOfAuthors());
                break;
            case NUM_OF_COMMITS:
                text = String.format("%s: %d", Constants.NUMBER_OF_COMMITS_TEXT, heatObject.getNumberOfCommits());
                break;
            case OVERALL:
                //Overall shows all of the above metrics
                text = String.format("%s: %d characters\n%s: %d\n%s: %d",
                        Constants.FILE_SIZE_TEXT, heatObject.getFileSize(),
                        Constants.NUMBER_OF_AUTHORS_TEXT, heatObject.getNumberOfAuthors(),
                        Constants.NUMBER_OF_COMMITS_TEXT, heatObject.getNumberOfCommits());
                break;
            default:
                throw new UnsupportedOperationException("Heat Metric Option is getHeatMetricString or not implemented for this method.");
        }
        return text;
    }

    @Override
    public boolean equals(Object object) {
        if (object != null && object.getClass() == getClass()) {
            FileObject fileObject = (FileObject) object;
            return this.getFilename().equals(fileObject.getFilename());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
