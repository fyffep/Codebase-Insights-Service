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
    private int degreeOfCoupling; //how many times it appeared in the same commit contiguity group with its coupled files
    private int buildFailureScore; //a measure of how often builds failed *recently*; higher is worse or indicative of more activity.
    private int cyclomaticComplexity;
    private int codeSmellScore;

    //Heat values
    private int fileSizeHeat; //combination of both lineCount and fileSize
    private int numberOfCommitsHeat;
    private int numberOfAuthorsHeat;
    private int degreeOfCouplingHeat;
    private int buildFailureScoreHeat;
    private int cyclomaticComplexityHeat;
    private int codeSmellScoreHeat;
    private double overallHeat;


    public HeatObject() {
        //This allows the metrics to be filled out gradually
        fileSizeHeat = Constants.HEAT_MIN;
        numberOfCommitsHeat = Constants.HEAT_MIN;
        numberOfAuthorsHeat = Constants.HEAT_MIN;
        degreeOfCouplingHeat = Constants.HEAT_MIN;
        buildFailureScoreHeat = Constants.HEAT_MIN;
        cyclomaticComplexityHeat = Constants.HEAT_MIN;
        codeSmellScoreHeat = Constants.HEAT_MIN;
        overallHeat = Constants.HEAT_MIN;
        constrainHeatLevel();

        filename = "";
        lineCount = -1;
        fileSize = -1;
        numberOfCommits = -1;
        numberOfAuthors = -1;
        degreeOfCoupling = -1;
        buildFailureScore = -1;
        cyclomaticComplexity = -1;
        codeSmellScore = -1;
    }

    public HeatObject(String filename, long lineCount, long fileSize, int numberOfCommits, int numberOfAuthors) {
        fileSizeHeat = Constants.HEAT_MIN;
        numberOfCommitsHeat = Constants.HEAT_MIN;
        numberOfAuthorsHeat = Constants.HEAT_MIN;
        degreeOfCouplingHeat = Constants.HEAT_MIN;
        buildFailureScoreHeat = Constants.HEAT_MIN;
        cyclomaticComplexityHeat = Constants.HEAT_MIN;
        codeSmellScoreHeat = Constants.HEAT_MIN;
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
            case BUILD_FAILURE_SCORE:
                return getBuildFailureScoreHeat();
            case CYCLOMATIC_COMPLEXITY:
                return getCyclomaticComplexityHeat();
            case CODE_SMELL_SCORE:
                return getCodeSmellScoreHeat();
            case OVERALL:
                return getOverallHeat();
            default:
                throw new UnsupportedOperationException(heatMetric + " is not supported for HeatObject::getHeatLevel(...)");
        }
    }
    public double getHeatLevel(Constants.HeatMetricOptionsExceptOverall heatMetric) {
        switch (heatMetric) {
            case FILE_SIZE:
                return getFileSizeHeat();
            case NUM_OF_COMMITS:
                return getNumberOfCommitsHeat();
            case NUM_OF_AUTHORS:
                return getNumberOfAuthorsHeat();
            case DEGREE_OF_COUPLING:
                return getDegreeOfCouplingHeat();
            case BUILD_FAILURE_SCORE:
                return getBuildFailureScoreHeat();
            case CYCLOMATIC_COMPLEXITY:
                return getCyclomaticComplexityHeat();
            case CODE_SMELL_SCORE:
                return getCodeSmellScoreHeat();
            default:
                throw new UnsupportedOperationException(heatMetric + " is not supported for HeatObject::getHeatLevel(...)");
        }
    }
    public void setHeatLevel(Constants.HeatMetricOptionsExceptOverall heatMetric, int newValue) {
        switch (heatMetric) {
            case FILE_SIZE:
                fileSizeHeat = newValue;
                break;
            case NUM_OF_COMMITS:
                numberOfCommitsHeat = newValue;
                break;
            case NUM_OF_AUTHORS:
                numberOfAuthorsHeat = newValue;
                break;
            case DEGREE_OF_COUPLING:
                degreeOfCouplingHeat = newValue;
                break;
            case BUILD_FAILURE_SCORE:
                buildFailureScoreHeat = newValue;
                break;
            case CYCLOMATIC_COMPLEXITY:
                cyclomaticComplexityHeat = newValue;
                break;
            case CODE_SMELL_SCORE:
                codeSmellScoreHeat = newValue;
                break;
            default:
                throw new UnsupportedOperationException(heatMetric + " is not supported for HeatObject::setHeatLevel(...)");
        }
        constrainHeatLevel();
    }
    public double getMetricValue(Constants.HeatMetricOptionsExceptOverall heatMetric) {
        switch (heatMetric) {
            case FILE_SIZE:
                return getFileSize(); //chosen over line count
            case NUM_OF_COMMITS:
                return getNumberOfCommits();
            case NUM_OF_AUTHORS:
                return getNumberOfAuthors();
            case DEGREE_OF_COUPLING:
                return getDegreeOfCoupling();
            case BUILD_FAILURE_SCORE:
                return getBuildFailureScore();
            case CYCLOMATIC_COMPLEXITY:
                return getCyclomaticComplexity();
            case CODE_SMELL_SCORE:
                return getCodeSmellScore();
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

    public int getBuildFailureScoreHeat() {
        return buildFailureScoreHeat;
    }

    public void setBuildFailureScoreHeat(int buildFailureScoreHeat) {
        this.buildFailureScoreHeat = buildFailureScoreHeat;
    }

    public int getCyclomaticComplexityHeat() {
        return cyclomaticComplexityHeat;
    }

    public void setCyclomaticComplexityHeat(int cyclomaticComplexityHeat) {
        this.cyclomaticComplexityHeat = cyclomaticComplexityHeat;
    }

    public int getCodeSmellScoreHeat() {
        return codeSmellScoreHeat;
    }

    public void setCodeSmellScoreHeat(int codeSmellScoreHeat) {
        this.codeSmellScoreHeat = codeSmellScoreHeat;
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

    public int getDegreeOfCoupling() {
        return degreeOfCoupling;
    }

    public void setDegreeOfCoupling(int degreeOfCoupling) {
        this.degreeOfCoupling = degreeOfCoupling;
    }

    public int getBuildFailureScore() {
        return buildFailureScore;
    }

    public void setBuildFailureScore(int buildFailureScore) {
        this.buildFailureScore = buildFailureScore;
    }

    public int getCyclomaticComplexity() {
        return cyclomaticComplexity;
    }

    public void setCyclomaticComplexity(int cyclomaticComplexity) {
        this.cyclomaticComplexity = cyclomaticComplexity;
    }

    public int getCodeSmellScore() {
        return codeSmellScore;
    }

    public void setCodeSmellScore(int codeSmellScore) {
        this.codeSmellScore = codeSmellScore;
    }

    private void constrainHeatLevel() {
        //For each heat value, adjust it so that it inside the min-max range
        fileSizeHeat = constrainHeatLevelHelper(fileSizeHeat);
        numberOfCommitsHeat = constrainHeatLevelHelper(numberOfCommitsHeat);
        numberOfAuthorsHeat = constrainHeatLevelHelper(numberOfAuthorsHeat);
        degreeOfCouplingHeat = constrainHeatLevelHelper(degreeOfCouplingHeat);
        buildFailureScoreHeat = constrainHeatLevelHelper(buildFailureScoreHeat);
        cyclomaticComplexityHeat = constrainHeatLevelHelper(cyclomaticComplexityHeat);
        codeSmellScoreHeat = constrainHeatLevelHelper(codeSmellScoreHeat);

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
