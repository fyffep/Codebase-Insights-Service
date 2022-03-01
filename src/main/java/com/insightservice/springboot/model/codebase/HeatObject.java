package com.insightservice.springboot.model.codebase;

import com.insightservice.springboot.Constants;

/**
 * Records a file's metrics and its heat level for the state
 * of that file at a particular Git commit.
 * filename uniquely identifies the file.
 */
public class HeatObject {

    private String filename;

    //Metrics
    private long lineCount;
    private long fileSize;
    private int numberOfCommits;
    private int numberOfAuthors;
    //private int degreeOfCoupling; //TODO; how many times it appeared in the same commit contiguity group with its coupled files
    //private double goodBadCommitRatio; //TODO; comes from Jenkins or other CI tool

    //Heat values
    private int fileSizeHeat; //combination of both lineCount and fileSize
    private int numberOfCommitsHeat;
    private int numberOfAuthorsHeat;
    private int degreeOfCouplingHeat; //to be implemented
    private int goodBadCommitRatioHeat; //to be implemented
    private double overallHeat;


    public HeatObject() {
        //This allows the metrics to be filled out gradually
        fileSizeHeat = Constants.HEAT_MIN;
        numberOfCommitsHeat = Constants.HEAT_MIN;
        numberOfAuthorsHeat = Constants.HEAT_MIN;
        degreeOfCouplingHeat = Constants.HEAT_MIN;
        goodBadCommitRatioHeat = Constants.HEAT_MIN;
        overallHeat = Constants.HEAT_MIN;
        constrainHeatLevel();

        filename = "";
        lineCount = -1;
        fileSize = -1;
        numberOfCommits = -1;
        numberOfAuthors = -1;
    }

    public HeatObject(int heatLevel, String filename, long lineCount, long fileSize, int numberOfCommits, int numberOfAuthors) {
        fileSizeHeat = Constants.HEAT_MIN;
        numberOfCommitsHeat = Constants.HEAT_MIN;
        numberOfAuthorsHeat = Constants.HEAT_MIN;
        degreeOfCouplingHeat = Constants.HEAT_MIN;
        goodBadCommitRatioHeat = Constants.HEAT_MIN;
        overallHeat = Constants.HEAT_MIN;
        constrainHeatLevel();

        this.filename = filename;
        this.lineCount = lineCount;
        this.fileSize = fileSize;
        this.numberOfCommits = numberOfCommits;
        this.numberOfAuthors = numberOfAuthors;
    }

    public double getHeatLevel(Constants.HeatMetricOptions heatMetric) {
        switch (heatMetric) {
            case FILE_SIZE:
                return getFileSizeHeat();
            case NUM_OF_COMMITS:
                return getNumberOfCommitsHeat();
            case NUM_OF_AUTHORS:
                return getNumberOfAuthorsHeat();
            case DEGREE_OF_COUPLING:
                return getDegreeOfCouplingHeat();
            case COMMIT_RATIO:
                return getGoodBadCommitRatioHeat();
            case OVERALL:
                return getOverallHeat();
            default:
                throw new UnsupportedOperationException(heatMetric + " is not supported for HeatObject::getHeatLevel(...)");
        }
    }

    public int getFileSizeHeat() {
        return fileSizeHeat;
    }

    public void setFileSizeHeat(int fileSizeHeat) {
        this.fileSizeHeat = fileSizeHeat;
        constrainHeatLevel();
    }

    public int getNumberOfCommitsHeat() {
        return numberOfCommitsHeat;
    }

    public void setNumberOfCommitsHeat(int numberOfCommitsHeat) {
        this.numberOfCommitsHeat = numberOfCommitsHeat;
        constrainHeatLevel();
    }

    public int getNumberOfAuthorsHeat() {
        return numberOfAuthorsHeat;
    }

    public void setNumberOfAuthorsHeat(int numberOfAuthorsHeat) {
        this.numberOfAuthorsHeat = numberOfAuthorsHeat;
        constrainHeatLevel();
    }

    public int getDegreeOfCouplingHeat() {
        return degreeOfCouplingHeat;
    }

    public void setDegreeOfCouplingHeat(int degreeOfCouplingHeat) {
        this.degreeOfCouplingHeat = degreeOfCouplingHeat;
        constrainHeatLevel();
    }

    public int getGoodBadCommitRatioHeat() {
        return goodBadCommitRatioHeat;
    }

    public void setGoodBadCommitRatioHeat(int goodBadCommitRatioHeat) {
        this.goodBadCommitRatioHeat = goodBadCommitRatioHeat;
        constrainHeatLevel();
    }

    public double getOverallHeat() {
        return overallHeat;
    }

    public void setOverallHeat(double overallHeat) {
        this.overallHeat = overallHeat;
        constrainHeatLevel();
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public long getLineCount() {
        return lineCount;
    }

    public void setLineCount(long lineCount) {
        this.lineCount = lineCount;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public int getNumberOfCommits() {
        return numberOfCommits;
    }

    public void setNumberOfCommits(int numberOfCommits) {
        this.numberOfCommits = numberOfCommits;
    }

    public int getNumberOfAuthors() {
        return numberOfAuthors;
    }

    public void setNumberOfAuthors(int numberOfAuthors) {
        this.numberOfAuthors = numberOfAuthors;
    }


    private void constrainHeatLevel() {
        //For each heat value, adjust it so that it inside the min-max range
        fileSizeHeat = constrainHeatLevelHelper(fileSizeHeat);
        numberOfCommitsHeat = constrainHeatLevelHelper(numberOfCommitsHeat);
        numberOfAuthorsHeat = constrainHeatLevelHelper(numberOfAuthorsHeat);
        degreeOfCouplingHeat = constrainHeatLevelHelper(degreeOfCouplingHeat);
        goodBadCommitRatioHeat = constrainHeatLevelHelper(goodBadCommitRatioHeat);

        //overall heat is the only double val
        if (this.overallHeat < Constants.HEAT_MIN)
            this.overallHeat = Constants.HEAT_MIN;
        else if (this.overallHeat > Constants.HEAT_MAX)
            this.overallHeat = Constants.HEAT_MAX;
    }

    private int constrainHeatLevelHelper(int heatInput) {
        if (heatInput < Constants.HEAT_MIN)
            return Constants.HEAT_MIN;
        else if (heatInput > Constants.HEAT_MAX)
            return Constants.HEAT_MAX;
        return heatInput; //else, no adjustment
    }
}
