package com.insightservice.springboot.model.codebase;

import com.insightservice.springboot.Constants;

import java.util.ArrayList;
import java.util.List;

public class DashboardModel
{
    //These lists correspond to Constants.HEAT_METRIC_OPTIONS and Constants.HeatMetricOptions to provide corresponding
    // averages or file names according to each heat metric.
    private ArrayList<Double> averageHeatScoreList;
    private ArrayList<String> namesOfHottestFileList;
    public final List<String> HEAT_METRIC_OPTIONS = Constants.HEAT_METRIC_OPTIONS; //this is here so that it is included when DashboardModel is converted into JSON

    public DashboardModel() {
        //Empty constructor
    }

    public ArrayList<Double> getAverageHeatScoreList() {
        return averageHeatScoreList;
    }

    public void setAverageHeatScoreList(ArrayList<Double> averageHeatScoreList) {
        this.averageHeatScoreList = averageHeatScoreList;
    }

    public ArrayList<String> getNamesOfHottestFileList() {
        return namesOfHottestFileList;
    }

    public void setNamesOfHottestFileList(ArrayList<String> namesOfHottestFileList) {
        this.namesOfHottestFileList = namesOfHottestFileList;
    }
}
