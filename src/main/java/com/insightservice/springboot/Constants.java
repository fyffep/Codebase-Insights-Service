package com.insightservice.springboot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Constants
{
    public static final Logger LOG = LoggerFactory.getLogger(Main.class);

    public static final String REPO_STORAGE_DIR = "repositories";
    public static final String JENKINS = "Jenkins";
    public static final String GITHUB_ACTIONS = "GitHub Actions";

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

    public enum HeatMetricOptions {
        OVERALL,
        FILE_SIZE,
        NUM_OF_COMMITS,
        NUM_OF_AUTHORS,
        DEGREE_OF_COUPLING,
        BUILD_FAILURE_SCORE,
        CYCLOMATIC_COMPLEXITY,
        CODE_SMELL_SCORE
    }
    public enum HeatMetricOptionsExceptOverall {
        FILE_SIZE,
        NUM_OF_COMMITS,
        NUM_OF_AUTHORS,
        DEGREE_OF_COUPLING,
        BUILD_FAILURE_SCORE,
        CYCLOMATIC_COMPLEXITY,
        CODE_SMELL_SCORE
    }

    // Heat
    // All minima/maxima are inclusive
    public static final int HEAT_MIN = 1;
    public static final int HEAT_MAX = 10;
    public static final int MIN_WEIGHT_ADJUSTMENT = -100;
    public static final int MAX_WEIGHT_ADJUSTMENT = 100;

    //DEFAULT Heat weights -- the actual heat weights are stored in the DB via the HeatWeights class
    public static final int HEAT_WEIGHT_TOTAL = 1000;
    public static final int WEIGHT_FILE_SIZE = 0; //combination of both lineCount and fileSize
    public static final int WEIGHT_NUM_OF_COMMITS = 300;
    public static final int WEIGHT_NUM_OF_AUTHORS = 300;
    public static final int WEIGHT_DEGREE_OF_COUPLING = 300;
    public static final int WEIGHT_BUILD_FAILURE_SCORE = 100;
    public static final int WEIGHT_CYCLOMATIC_COMPLEXITY = 0; //to be implemented
    public static final int WEIGHT_CODE_SMELL_SCORE = 0; //to be implemented

    public static final String SEPARATOR = "~";
    public static final String NO_FILES_EXIST = "No files exist";


    public static final int SCORE_PENALTY_AT_BUILD_FAILURE = 10; //how much HeatObject.buildFailureScore increases for each build failure

    //Prevent instantiation
    private Constants() {
    }
}
