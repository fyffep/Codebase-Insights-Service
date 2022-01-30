package com.insightservice.springboot;

public class Constants
{
    public static final String REPO_STORAGE_DIR = "repositories";

    // Default Branches
    public static final String[] DEFAULT_BRANCHES = {"development", "master", "main"};

    public static final GroupingMode DEFAULT_GROUPING = GroupingMode.PACKAGES;
    public enum GroupingMode {
        COMMITS,
        PACKAGES
    }

    // Heat
    public static final int HEAT_MIN = 1;
    public static final int HEAT_MAX = 10;


    //Prevent instantiation
    private Constants() {
    }
}
