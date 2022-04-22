package intellij_extension.model.machinelearning;

import com.insightservice.springboot.Constants;
import com.insightservice.springboot.Constants.HeatMetricOptionsExceptOverall;
import com.insightservice.springboot.model.machinelearning.HeatWeights;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HeatWeightsTest
{
    @Test
    void constructor_ValidateDefaultWeights()
    {
        HeatWeights heatWeights = new HeatWeights(); //method being tested

        assertEquals(Constants.WEIGHT_BUILD_FAILURE_SCORE,
                heatWeights.getMetricNameToWeightMap().get(HeatMetricOptionsExceptOverall.BUILD_FAILURE_SCORE));
        assertEquals(Constants.WEIGHT_CYCLOMATIC_COMPLEXITY,
                heatWeights.getMetricNameToWeightMap().get(HeatMetricOptionsExceptOverall.CYCLOMATIC_COMPLEXITY));
        assertEquals(Constants.WEIGHT_CODE_SMELL_SCORE,
                heatWeights.getMetricNameToWeightMap().get(HeatMetricOptionsExceptOverall.CODE_SMELL_SCORE));
        assertEquals(Constants.WEIGHT_DEGREE_OF_COUPLING,
                heatWeights.getMetricNameToWeightMap().get(HeatMetricOptionsExceptOverall.DEGREE_OF_COUPLING));
        assertEquals(Constants.WEIGHT_FILE_SIZE,
                heatWeights.getMetricNameToWeightMap().get(HeatMetricOptionsExceptOverall.FILE_SIZE));
        assertEquals(Constants.WEIGHT_NUM_OF_COMMITS,
                heatWeights.getMetricNameToWeightMap().get(HeatMetricOptionsExceptOverall.NUM_OF_COMMITS));
        assertEquals(Constants.WEIGHT_NUM_OF_AUTHORS,
                heatWeights.getMetricNameToWeightMap().get(HeatMetricOptionsExceptOverall.NUM_OF_AUTHORS));
    }

    @Test
    void constructor_MetricCount()
    {
        //If you fail this, a new heat metric was added but the constructor wasn't updated.

        HeatWeights heatWeights = new HeatWeights();
        for (HeatMetricOptionsExceptOverall metric : HeatMetricOptionsExceptOverall.values())
        {
            //Ensure each metric exists as a key in the HeatWeights map.
            assertTrue(heatWeights.getMetricNameToWeightMap().containsKey(metric));
        }
        //Compare the total number of metrics to the number of metrics given default values
        assertEquals(heatWeights.getMetricNameToWeightMap().keySet().size(), HeatMetricOptionsExceptOverall.values().length);
    }
}
