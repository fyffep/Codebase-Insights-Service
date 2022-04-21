package intellij_extension;

import com.insightservice.springboot.Constants;
import com.insightservice.springboot.model.codebase.Codebase;
import com.insightservice.springboot.model.codebase.Commit;
import com.insightservice.springboot.model.codebase.FileObject;
import com.insightservice.springboot.model.codebase.HeatObject;
import com.insightservice.springboot.utility.HeatCalculationUtility;
import org.junit.Before;
import org.junit.Test;
import testdata.TestData;

import java.nio.file.Path;

import static org.junit.Assert.*;

public class HeatCalculationUtilityTest
{
    @Test
    public void assignHeatLevelsRelativeToAverageTest_BUILD_FAILURE_SCORE()
    {
        TestData.setupMockCodebase();

        HeatCalculationUtility.assignHeatLevelsRelativeToAverage(TestData.mockCodebase,
                Constants.HeatMetricOptionsExceptOverall.BUILD_FAILURE_SCORE); //method under test

        //Assertions...
        assertTrue(TestData.heatObject1A.getBuildFailureScoreHeat() == 7);
        assertTrue(TestData.heatObject1B.getBuildFailureScoreHeat() == 0);
        assertTrue(TestData.heatObject1C.getBuildFailureScoreHeat() == 5);
    }

    @Test
    public void assignHeatLevelsRelativeToAverageTest_CYCLOMATIC_COMPLEXITY()
    {
        TestData.setupMockCodebase();

        HeatCalculationUtility.assignHeatLevelsRelativeToAverage(TestData.mockCodebase,
                Constants.HeatMetricOptionsExceptOverall.CYCLOMATIC_COMPLEXITY); //method under test

        //Assertions...
        assertTrue(TestData.heatObject1A.getCyclomaticComplexityHeat() == 8);
        assertTrue(TestData.heatObject1B.getCyclomaticComplexityHeat() == 3);
        assertTrue(TestData.heatObject1C.getCyclomaticComplexityHeat() == 1);
    }
}
