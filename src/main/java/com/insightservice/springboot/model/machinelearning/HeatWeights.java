package com.insightservice.springboot.model.machinelearning;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.insightservice.springboot.Constants;
import com.insightservice.springboot.Constants.HeatMetricOptionsExceptOverall;
import org.springframework.data.annotation.Id;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.HashMap;


/**
 * For each heat metric, we assign an integer weight to control how much it contributes to each a file's heat.
 * There is only one HeatWeights **sum** for the entire system, and it is stored on the database with ID -1.
 * All other HeatWeights instances are used to record adjustments to this total.
 * The weights may be adjusted through the HashMap<HeatMetricOptionsExceptOverall, Integer> metricNameToWeightMap.
 */
public class HeatWeights
{
    @Id
    @JsonIgnore
    private String id;
    //HeatWeights instance #-1 is the only one that records the sum of all adjustments.
    public static final String ID_OF_ADJUSTMENT_TOTAL = "-1";

    //The weights in this map should add up to roughly Constants.HEAT_WEIGHT_TOTAL (i.e. 1000 at the time of writing this)
    //...but this is not enforced anywhere as a requirement.
    private HashMap<HeatMetricOptionsExceptOverall, Integer> metricNameToWeightMap;

    //These fields will be null if the instance has ID -1.
    //Otherwise, they record where an adjustment took place.
    private String commitHash;
    private String fileName;
    private String gitHubUrl;


    public HeatWeights() {
        //Assign default values
        metricNameToWeightMap = new HashMap<>();
        metricNameToWeightMap.put(HeatMetricOptionsExceptOverall.FILE_SIZE, Constants.WEIGHT_FILE_SIZE);
        metricNameToWeightMap.put(HeatMetricOptionsExceptOverall.NUM_OF_COMMITS, Constants.WEIGHT_NUM_OF_COMMITS);
        metricNameToWeightMap.put(HeatMetricOptionsExceptOverall.NUM_OF_AUTHORS, Constants.WEIGHT_NUM_OF_AUTHORS);
        metricNameToWeightMap.put(HeatMetricOptionsExceptOverall.DEGREE_OF_COUPLING, Constants.WEIGHT_DEGREE_OF_COUPLING);
        metricNameToWeightMap.put(HeatMetricOptionsExceptOverall.BUILD_FAILURE_SCORE, Constants.WEIGHT_BUILD_FAILURE_SCORE);
        metricNameToWeightMap.put(HeatMetricOptionsExceptOverall.CYCLOMATIC_COMPLEXITY, Constants.WEIGHT_CYCLOMATIC_COMPLEXITY);
        metricNameToWeightMap.put(HeatMetricOptionsExceptOverall.CODE_SMELL_SCORE, Constants.WEIGHT_CODE_SMELL_SCORE);
    }


    //region getters/setters
    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public HashMap<HeatMetricOptionsExceptOverall, Integer> getMetricNameToWeightMap() {
        return metricNameToWeightMap;
    }

    public void setMetricNameToWeightMap(HashMap<HeatMetricOptionsExceptOverall, Integer> metricNameToWeightMap) {
        this.metricNameToWeightMap = metricNameToWeightMap;
    }

    public String getCommitHash() {
        return commitHash;
    }

    public void setCommitHash(String commitHash) {
        this.commitHash = commitHash;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getGitHubUrl() {
        return gitHubUrl;
    }

    public void setGitHubUrl(String gitHubUrl) {
        this.gitHubUrl = gitHubUrl;
    }

    //endregion
}
