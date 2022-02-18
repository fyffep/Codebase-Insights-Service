package com.insightservice.springboot.utility;

import com.insightservice.springboot.Constants;
import com.insightservice.springboot.model.codebase.Codebase;
import com.insightservice.springboot.model.codebase.DashboardModel;
import com.insightservice.springboot.model.codebase.FileObject;
import com.insightservice.springboot.model.codebase.HeatObject;

import java.util.ArrayList;

public class DashboardCalculationUtility
{
    private DashboardCalculationUtility() {
        //This is a utility class
    }

    /**
     * Returns the average heat level for latest commit in the Codebase.
     * Rounds the output value to the nearest 10th place.
     */
    public static double averageHeatLevel(Codebase codebase, Constants.HeatMetricOptions heatMetricOption)
    {
        //Compute total amount of heat across all files at the latest commit
        long heatSum = 0;
        int numberOfFiles = 0;
        String latestCommitHash = codebase.getLatestCommitHash();
        for (FileObject fileObject : codebase.getActiveFileObjects())
        {
            HeatObject heatObject = fileObject.getHeatObjectAtCommit(latestCommitHash);

            if (heatObject == null) continue; //file was not a part of the commit

            heatSum += heatObject.getHeatLevel(heatMetricOption);
            numberOfFiles++;
        }

        //Compute average heat
        double heatAverage = (double)(heatSum) / numberOfFiles;
        heatAverage = Math.round(heatAverage * 10) / 10.0; //round to nearest 10th decimal place
        return heatAverage;
    }


    /**
     * Summarizes the data in a Codebase into a DashboardModel.
     * The DashboardModel is given all the data it needs so that it can
     * be displayed immediately.
     * @param codebase contains the data from analyzing a Codebase.
     * @return a DashboardModel with average heat scores and the names of the #1 hottest files
     * from each metric.
     */
    public static DashboardModel assignDashboardData(Codebase codebase)
    {
        //Compute every file's heat
        HeatCalculationUtility.assignHeatLevels(codebase);

        //Compute average heat scores and the hottest files for each metric
        ArrayList<Double> averageHeatScoreList = new ArrayList<>();
        ArrayList<String> namesOfHottestFileList = new ArrayList<>();
        for (Constants.HeatMetricOptions heatMetricOption : Constants.HeatMetricOptions.values())
        {
            //Determine average heat for the metric
            double average = averageHeatLevel(codebase, heatMetricOption);
            averageHeatScoreList.add(average);

            //Determine hottest file for the metric
            namesOfHottestFileList.add(findHottestFile(codebase, heatMetricOption));
        }

        //Place the data into the DashboardModel
        DashboardModel dashboardModel = new DashboardModel();
        dashboardModel.setAverageHeatScoreList(averageHeatScoreList);
        dashboardModel.setNamesOfHottestFileList(namesOfHottestFileList);

        return dashboardModel;
    }

    /**
     * Assuming assignHeatLevels() has already been called, returns
     * the name of the file with the most heat. Many files can have the highest heat, however.
     * If there are no files in the Codebase, returns "No files exist".
     */
    private static String findHottestFile(Codebase codebase, Constants.HeatMetricOptions heatMetricOption)
    {
        String latestCommitHash = codebase.getLatestCommitHash();

        //Determine max heat
        double highestHeat = Double.MIN_VALUE;
        String nameOfHottestFile = Constants.NO_FILES_EXIST;
        for (FileObject fileObject : codebase.getActiveFileObjects())
        {
            HeatObject heatObject = fileObject.getHeatObjectAtCommit(latestCommitHash);

            if (heatObject == null) continue; //file was not a part of the commit

            if (heatObject.getHeatLevel(heatMetricOption) > highestHeat) {
                highestHeat = heatObject.getHeatLevel(heatMetricOption);
                nameOfHottestFile = fileObject.getFilename();
            }
        }

        return nameOfHottestFile;
    }
}
