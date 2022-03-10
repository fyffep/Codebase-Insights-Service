package com.insightservice.springboot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

public class Constants
{
    public static final Logger LOG = LoggerFactory.getLogger(Main.class);

    public static final String REPO_STORAGE_DIR = "repositories";

    // Default Branches
    public static final String[] DEFAULT_BRANCHES = {"development", "master", "main"};
    public static final String USE_DEFAULT_BRANCH = ""; //if user enters this, they don't want to choose a specific branch

    //Unused?
    public static final GroupingMode DEFAULT_GROUPING = GroupingMode.PACKAGES;
    public enum GroupingMode {
        COMMITS,
        PACKAGES
    }

    //Unused
//    public static final FilterMode DEFAULT_FILTERING = FilterMode.X_FILES;
//    public enum FilterMode {
//        ALL_FILES,
//        X_FILES
//    }

    // Heat Metric List
    // Note that this affects HeatMapController::newHeatMetricSelected()
    public static final String OVERALL_TEXT = "Overall Heat";
    public static final String FILE_SIZE_TEXT = "File Size";
    public static final String NUMBER_OF_COMMITS_TEXT = "Number of Commits";
    public static final String NUMBER_OF_AUTHORS_TEXT = "Number of Authors";
    public static final String DEGREE_OF_COUPLING_TEXT = "Degree of Coupling to Related Files";
    public static final String COMMIT_RATIO_TEXT = "Build Failure Commit Ratio";
    public static final List<String> HEAT_METRIC_OPTIONS = Arrays.asList(
            OVERALL_TEXT,
            FILE_SIZE_TEXT,
            NUMBER_OF_COMMITS_TEXT,
            NUMBER_OF_AUTHORS_TEXT,
            DEGREE_OF_COUPLING_TEXT,
            COMMIT_RATIO_TEXT
    );
    // !!!
    //IMPORTANT: Make sure the HEAT_METRIC_OPTIONS and HeatMetricOptions correspond
    //because other code (the DashboardModel) iterates through them both with this assumption.
    // !!!
    public enum HeatMetricOptions {
        OVERALL,
        FILE_SIZE,
        NUM_OF_COMMITS,
        NUM_OF_AUTHORS,
        DEGREE_OF_COUPLING,
        COMMIT_RATIO,
        CYCLOMATIC_COMPLEXITY
    }
    public enum HeatMetricOptionsExceptOverall {
        FILE_SIZE,
        NUM_OF_COMMITS,
        NUM_OF_AUTHORS,
        DEGREE_OF_COUPLING,
        COMMIT_RATIO,
        CYCLOMATIC_COMPLEXITY
    }

    // Heat
    // All minima/maxima are inclusive
    public static final int HEAT_MIN = 1;
    public static final int HEAT_MAX = 10;
    public static final double MIN_WEIGHT_ADJUSTMENT = -0.05;
    public static final double MAX_WEIGHT_ADJUSTMENT = 0.05;

    //DEFAULT Heat weights -- the actual heat weights are stored in the DB via the HeatWeights class
    public static final double WEIGHT_FILE_SIZE = 0.0; //combination of both lineCount and fileSize
    public static final double WEIGHT_NUM_OF_COMMITS = 0.5;
    public static final double WEIGHT_NUM_OF_AUTHORS = 0.5;
    public static final double WEIGHT_DEGREE_OF_COUPLING = 0.0; //to be implemented
    public static final double WEIGHT_COMMIT_RATIO = 0.0; //to be implemented
    public static final double WEIGHT_CYCLOMATIC_COMPLEXITY = 0.0; //to be implemented

    public static final String SEPARATOR = "~";
    public static final String NO_FILES_EXIST = "No files exist";

    //Prevent instantiation
    private Constants() {
    }
}
