package com.insightservice.springboot.model.machinelearning;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.insightservice.springboot.Constants;
import com.insightservice.springboot.Constants.HeatMetricOptionsExceptOverall;
import org.springframework.data.annotation.Id;

import java.util.HashMap;


/**
 * For each heat metric, we assign an integer weight to control how much it contributes to each a file's heat.
 * There is only one HeatWeights instance for the entire system, and it is storied on the database with ID 0.
 * The weights may be adjusted through the HashMap<HeatMetricOptionsExceptOverall, Integer> metricNameToWeightMap.
 */
public class HeatWeights
{
    @Id
    @JsonIgnore
    private String singletonId; //always takes value of SINGLETON_ID. The weird syntax here is needed for MongoDB.
    public static final String SINGLETON_ID = "0";

    //The weights in this map should add up to roughly Constants.HEAT_WEIGHT_TOTAL (i.e. 1000 at the time of writing this)
    //...but this is not enforced anywhere as a requirement.
    private HashMap<HeatMetricOptionsExceptOverall, Integer> metricNameToWeightMap;


    public HeatWeights() {
        //Assign default values
        metricNameToWeightMap = new HashMap<>();
        metricNameToWeightMap.put(HeatMetricOptionsExceptOverall.FILE_SIZE, Constants.WEIGHT_FILE_SIZE);
        metricNameToWeightMap.put(HeatMetricOptionsExceptOverall.NUM_OF_COMMITS, Constants.WEIGHT_NUM_OF_COMMITS);
        metricNameToWeightMap.put(HeatMetricOptionsExceptOverall.NUM_OF_AUTHORS, Constants.WEIGHT_NUM_OF_AUTHORS);
        metricNameToWeightMap.put(HeatMetricOptionsExceptOverall.DEGREE_OF_COUPLING, Constants.WEIGHT_DEGREE_OF_COUPLING);
        metricNameToWeightMap.put(HeatMetricOptionsExceptOverall.COMMIT_RATIO, Constants.WEIGHT_COMMIT_RATIO);
        metricNameToWeightMap.put(HeatMetricOptionsExceptOverall.CYCLOMATIC_COMPLEXITY, Constants.WEIGHT_CYCLOMATIC_COMPLEXITY);

        singletonId = SINGLETON_ID;
    }


    //region getters/setters
    /**
     * Always returns HeatWeights.SINGLETON_ID.
     * Only needed for MongoDB.
     */
    public String getSingletonId() {
        return SINGLETON_ID;
    }

    /**
     * Always sets the value to HeatWeights.SINGLETON_ID.
     * Only needed for MongoDB.
     * @param singletonId IGNORED
     */
    public void setSingletonId(String singletonId) {
        this.singletonId = SINGLETON_ID;
    }

    public HashMap<HeatMetricOptionsExceptOverall, Integer> getMetricNameToWeightMap() {
        return metricNameToWeightMap;
    }

    public void setMetricNameToWeightMap(HashMap<HeatMetricOptionsExceptOverall, Integer> metricNameToWeightMap) {
        this.metricNameToWeightMap = metricNameToWeightMap;
    }
    //endregion
}
