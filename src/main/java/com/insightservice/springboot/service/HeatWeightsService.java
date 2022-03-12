package com.insightservice.springboot.service;

import com.insightservice.springboot.Constants;
import com.insightservice.springboot.Constants.HeatMetricOptionsExceptOverall;
import com.insightservice.springboot.model.machinelearning.HeatWeights;
import com.insightservice.springboot.repository.HeatWeightsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;


@Service
public class HeatWeightsService 
{
    @Autowired
    HeatWeightsRepository heatWeightsRepository;

    /**
     * Adjusts the heat weights on the DB
     * @param heatWeightAdjustments the suggestions to adjust the heat weights.
     *            Each weight value (adjustment) must be in the range [-0.05, 0.05]
     * @return the newly-updated HeatWeights
     */
    public HeatWeights storeHeatWeightAdjustments(HeatWeights heatWeightAdjustments)
    {
        HashMap<HeatMetricOptionsExceptOverall, Integer> adjustmentMap =  heatWeightAdjustments.getMetricNameToWeightMap();
        if (adjustmentMap == null)
            throw new NullPointerException("A heatWeightAdjustments needs a heat metric to weight adjustment map to be valid.");

        //Get or create existing HeatWeights from the DB
        HeatWeights existingHeatWeights = heatWeightsRepository.findById(HeatWeights.SINGLETON_ID)
                .orElse(new HeatWeights());
        HashMap<HeatMetricOptionsExceptOverall, Integer> weightMapToUpdate = existingHeatWeights.getMetricNameToWeightMap();

        //Validate the adjustments
        for (HeatMetricOptionsExceptOverall metric : HeatMetricOptionsExceptOverall.values())
        {
            if (adjustmentMap.containsKey(metric))
            {
                //Constrain the min/max
                double adjustment = adjustmentMap.get(metric);
                if (adjustment < Constants.MIN_WEIGHT_ADJUSTMENT)
                    adjustmentMap.put(metric, Constants.MIN_WEIGHT_ADJUSTMENT);
                else if (adjustment > Constants.MAX_WEIGHT_ADJUSTMENT)
                    adjustmentMap.put(metric, Constants.MAX_WEIGHT_ADJUSTMENT);
            }
            else
            {
                adjustmentMap.put(metric, 0); //Assume no adjustment
            }
        }

        //Calculate and store the adjustments
        for (HeatMetricOptionsExceptOverall metric : HeatMetricOptionsExceptOverall.values())
        {
            int oldWeight = weightMapToUpdate.get(metric);
            int newWeight = oldWeight + adjustmentMap.get(metric);
            weightMapToUpdate.put(metric, newWeight);
        }

        //Now existingHeatWeights's weightMapToUpdate has the new heat weights.

        //Save to DB
        heatWeightsRepository.save(existingHeatWeights);

        return existingHeatWeights;
    }
}
