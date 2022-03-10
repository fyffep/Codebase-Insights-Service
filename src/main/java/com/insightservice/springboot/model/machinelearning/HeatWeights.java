package com.insightservice.springboot.model.machinelearning;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.insightservice.springboot.Constants;
import com.insightservice.springboot.Constants.HeatMetricOptionsExceptOverall;
import org.springframework.data.annotation.Id;

import java.util.HashMap;


public class HeatWeights
{
    @Id
    @JsonIgnore
    private String singletonId; //always takes value of SINGLETON_ID. The weird syntax here is needed for MongoDB.
    public static final String SINGLETON_ID = "0";

    private HashMap<HeatMetricOptionsExceptOverall, Double> metricNameToWeightMap;


    public HeatWeights() {
        //Assign default values
        metricNameToWeightMap = new HashMap<>();
        metricNameToWeightMap.put(HeatMetricOptionsExceptOverall.FILE_SIZE, Constants.WEIGHT_FILE_SIZE);
        metricNameToWeightMap.put(HeatMetricOptionsExceptOverall.NUM_OF_COMMITS, Constants.WEIGHT_NUM_OF_COMMITS);
        metricNameToWeightMap.put(HeatMetricOptionsExceptOverall.NUM_OF_AUTHORS, Constants.WEIGHT_NUM_OF_AUTHORS);
        metricNameToWeightMap.put(HeatMetricOptionsExceptOverall.DEGREE_OF_COUPLING, Constants.WEIGHT_DEGREE_OF_COUPLING);
        metricNameToWeightMap.put(HeatMetricOptionsExceptOverall.COMMIT_RATIO, Constants.WEIGHT_COMMIT_RATIO);
        metricNameToWeightMap.put(HeatMetricOptionsExceptOverall.CYCLOMATIC_COMPLEXITY, Constants.WEIGHT_CYCLOMATIC_COMPLEXITY);

        //If you fail this, a new heat metric was added but this constructor wasn't updated
        // TODO make a unit test
        for (HeatMetricOptionsExceptOverall metric : HeatMetricOptionsExceptOverall.values())
        {
            assert metricNameToWeightMap.containsKey(metric);
        }
        if (metricNameToWeightMap.keySet().size() != HeatMetricOptionsExceptOverall.values().length)
            throw new IllegalStateException("Default heat weights were not configured properly");

        forceToAddUpTo1();
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
     * @param singletonId ignored
     */
    public void setSingletonId(String singletonId) {
        this.singletonId = SINGLETON_ID;
    }

    public HashMap<HeatMetricOptionsExceptOverall, Double> getMetricNameToWeightMap() {
        return metricNameToWeightMap;
    }

    public void setMetricNameToWeightMap(HashMap<HeatMetricOptionsExceptOverall, Double> metricNameToWeightMap) {
        this.metricNameToWeightMap = metricNameToWeightMap;
    }
    //endregion


    public void forceToAddUpTo1()
    {
        if (this.getMetricNameToWeightMap().keySet().size() != HeatMetricOptionsExceptOverall.values().length)
            throw new IllegalStateException("Not every heat metric was mapped to a weight value");


        //region Calculate current weightSum and validate current weights
        double weightSum = 0.0;
        for (HeatMetricOptionsExceptOverall metric : HeatMetricOptionsExceptOverall.values()) {
            //Calculate total weightSum
            weightSum += metricNameToWeightMap.get(metric);
        }
        //endregion

        //region Ensure all weights add up to approximately 100% (= 1)
        if (weightSum < 1.0)
        {
            double weightToDistribute = (1.0 - weightSum) / HeatMetricOptionsExceptOverall.values().length;
            for (HeatMetricOptionsExceptOverall metric : HeatMetricOptionsExceptOverall.values())
            {
                double weight = metricNameToWeightMap.get(metric);
                metricNameToWeightMap.put(metric, weight + weightToDistribute);
            }
        }
        else if (weightSum > 1.0)
        {
            double weightToRemove = (weightSum - 1.0) / HeatMetricOptionsExceptOverall.values().length;
            for (HeatMetricOptionsExceptOverall metric : HeatMetricOptionsExceptOverall.values())
            {
                double weight = metricNameToWeightMap.get(metric);
                metricNameToWeightMap.put(metric, weight - weightToRemove);
            }
        }
        //endregion
    }
}
