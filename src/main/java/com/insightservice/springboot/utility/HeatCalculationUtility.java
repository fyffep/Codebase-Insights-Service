package com.insightservice.springboot.utility;

import com.insightservice.springboot.Constants;
import com.insightservice.springboot.model.codebase.Codebase;
import com.insightservice.springboot.model.codebase.Commit;
import com.insightservice.springboot.model.codebase.FileObject;
import com.insightservice.springboot.model.codebase.HeatObject;

import java.util.*;

import static com.insightservice.springboot.Constants.*;

/**
 * Modifies the Codebase so that every HeatObject within it is given a heat level
 * according to the specified metric.
 * assignHeatLevels() is the entry point of this class, and it is capable of giving every
 * HeatObject within a Codebase heat levels according to the given metric.
 */
public class HeatCalculationUtility
{
    private HeatCalculationUtility() {
        //This is a utility class
    }


    public static void assignHeatLevelsRelativeToAverage(Codebase codebase, HeatMetricOptionsExceptOverall heatMetric)
    {
        /*
        The target heat metric is computed based on how many standard deviations a metric value is
        away from the average. Only the latest commit is considered.
         */
        final double STD_DEVS_FOR_MAX_HEAT = 2.0; //A metric must be at least 2 standard deviations higher than the mean to achieve max heat.
        final double HEAT_FOR_BEING_AVERAGE = 4.0; //If a metric is exactly equal to the mean, its heat will be this.
        LOG.info("Calculating heat based on "+heatMetric+"...");

        Set<FileObject> fileObjectSet = codebase.getActiveFileObjects();

        if (fileObjectSet.size() <= 0) {
            LOG.error("assignHeatLevelsRelativeToAverage(...) failed. 0 files were found in the codebase.");
            return;
        }

        //Compute average
        String commitHash = codebase.getLatestCommitHash();
        double sum = 0;
        for (FileObject fileObject : fileObjectSet)
        {
            HeatObject heatObject = fileObject.getHeatObjectAtCommit(commitHash);
            if (heatObject != null)
                sum += heatObject.getMetricValue(heatMetric);
            else
                LOG.error("No HeatObject exists for file `"+fileObject.getFilename()+"` at commit "+commitHash);
        }
        final double average = sum / fileObjectSet.size();
        LOG.info("Average metric value at commit "+commitHash+" is "+average);

        //Compute standard deviation
        double sumOfSquaredDifferences = 0;
        for (FileObject fileObject : fileObjectSet)
        {
            HeatObject heatObject = fileObject.getHeatObjectAtCommit(commitHash);
            if (heatObject != null)
                sumOfSquaredDifferences += Math.pow(heatObject.getMetricValue(heatMetric) - average, 2.0);
            else
                LOG.error("No HeatObject exists for file `"+fileObject.getFilename()+"` at commit "+commitHash);
        }
        final double standardDeviation = Math.sqrt(sumOfSquaredDifferences / fileObjectSet.size());
        LOG.info("Average value of "+heatMetric+" at commit "+commitHash+" is "+average+" with standard deviation "+standardDeviation);

        final double valueNeededForMaxHeat = average + (STD_DEVS_FOR_MAX_HEAT * standardDeviation); //A metric must be >= this value to attain max heat.
        System.out.println("valueNeededForMaxHeat="+valueNeededForMaxHeat);

        //Determine heat based on number of standard deviations away from the mean
        for (FileObject fileObject : fileObjectSet)
        {
            HeatObject heatObject = fileObject.getHeatObjectAtCommit(commitHash);
            if (heatObject != null)
            {
                double metricValue = heatObject.getMetricValue(heatMetric);

                //Linear growth
                double stdDevsAwayFromMean = (metricValue - average) / standardDeviation;
                int heatLevel = (int) Math.round(
                        ((HEAT_MAX - HEAT_FOR_BEING_AVERAGE) / 2.0) * stdDevsAwayFromMean + HEAT_FOR_BEING_AVERAGE
                );

                //Exponential growth
                //final double fixedPart = Math.pow(Constants.HEAT_MAX, 1.0 / (STD_DEVS_FOR_MAX_HEAT - 1));
                //int heatLevel = (int)((1.0 / fixedPart) * Math.pow(fixedPart, stdDevsAwayFromMean));

                heatObject.setHeatLevel(heatMetric, heatLevel);
                //System.out.println("Assigned file `"+fileObject.getFilename()+"` heat "+heatLevel+" because it has value "+metricValue +" which is "+stdDevsAwayFromMean+" stdDevsAwayFromMean");
            }
            else
                LOG.error("No HeatObject exists for file `"+fileObject.getFilename()+"` at commit "+commitHash);
        }
        LOG.info("Finished calculating heat based on "+heatMetric);
    }




    private static void assignHeatLevelsFileSize(Codebase codebase)
    {
        LOG.info("Calculating heat based on file size...");
        final int REQUIRED_NUM_COMMITS_WITHOUT_CHANGING = 5; //the number of consecutive commits where no increase in a file's size is recorded needed in order to reduce the accumulated heat level.
        final int REQUIRED_SIZE_CHANGE = 200;
        final int SIZE_INCREASE_HEAT_CONSEQUENCE = 2; //how much the heat increases when the file size increases
        final int SIZE_DECREASE_HEAT_CONSEQUENCE = -1; //how much the heat decreases when the file size decreases
        final int SIZE_NO_CHANGE_HEAT_CONSEQUENCE = -1; //how much the heat decreases if the file size stays the same for long enough

        Set<FileObject> fileObjectSet = codebase.getActiveFileObjects();
        for (FileObject fileObject : fileObjectSet)
        {
            //The oldest commits are at the front of the LinkedHashMap
            LinkedHashMap<String, HeatObject> commitHashToHeatObjectMap = fileObject.getCommitHashToHeatObjectMap();

            HeatObject lastHeatObject = null;
            int numberOfConsecutiveCommitsWithNoSizeIncrease = 0;

            for (Map.Entry<String, HeatObject> commitToHeatObjectEntry : commitHashToHeatObjectMap.entrySet())
            {
                HeatObject newerHeatObject = commitToHeatObjectEntry.getValue();
                if (lastHeatObject != null)
                {
                    //Compute the heat based on how much the size has changed over time
                    int accumulatedHeatLevel = lastHeatObject.getFileSizeHeat(); //use previous heat, then modify

                    //If the file size increased at all, incur 2 heat
                    long oldFileSize = lastHeatObject.getFileSize();
                    long newFileSize = newerHeatObject.getFileSize();

                    //File size increase -> gain heat
                    if (newFileSize > oldFileSize)
                    {
                        accumulatedHeatLevel += SIZE_INCREASE_HEAT_CONSEQUENCE;
                        numberOfConsecutiveCommitsWithNoSizeIncrease = 0;
                    }
                    //File size decrease -> lose heat
                    else if (oldFileSize - newFileSize >= REQUIRED_SIZE_CHANGE)
                    {
                        accumulatedHeatLevel += SIZE_DECREASE_HEAT_CONSEQUENCE;
                        numberOfConsecutiveCommitsWithNoSizeIncrease++;
                    }
                    //File size stayed equal
                    else
                    {
                        numberOfConsecutiveCommitsWithNoSizeIncrease++;

                        //If file went unchanged for long enough, the heat improved
                        if (numberOfConsecutiveCommitsWithNoSizeIncrease >= REQUIRED_NUM_COMMITS_WITHOUT_CHANGING)
                        {
                            accumulatedHeatLevel += SIZE_NO_CHANGE_HEAT_CONSEQUENCE;
                            numberOfConsecutiveCommitsWithNoSizeIncrease = 0;
                        }
                    }

                    //Now compute the heat based on the changes in size, current file size, and line count.
                    //Then average all measurements.
                    double heatLevel = MathUtility.average(
                            accumulatedHeatLevel,
                            computeFileSizeHeat(newerHeatObject.getFileSize()),
                            computeLineCountHeat(newerHeatObject.getLineCount()));

                    //Store the heat
                    newerHeatObject.setFileSizeHeat((int) Math.round( heatLevel ));
                }
                else
                {
                    //No in/decreases in file size yet:
                    //Compute the heat based on the current file size & line count, then average all measurements
                    double heatLevel = MathUtility.average(
                            computeFileSizeHeat(newerHeatObject.getFileSize()),
                            computeLineCountHeat(newerHeatObject.getLineCount()));

                    //Store the heat
                    newerHeatObject.setFileSizeHeat((int) Math.round( heatLevel ));
                }
                lastHeatObject = newerHeatObject;
            }
        }
        LOG.info("Finished calculating heat based on file size.");
    }

    private static int computeFileSizeHeat(long fileSize)
    {
        if (fileSize < 0)
            throw new IllegalArgumentException("computeFileSizeHeat() refuses to handle negative line count input `" + fileSize + "`");

        //Use exponential function to calculate heat
        final int CHARACTERS_FOR_MAX_HEAT = 10000;
        double fixedPart = Math.pow(Constants.HEAT_MAX, 1.0 / (CHARACTERS_FOR_MAX_HEAT - 1));
        int heatLevel = (int)((1.0 / fixedPart) * Math.pow(fixedPart, fileSize));

        //If the result is too large, constrain it
        if (heatLevel > Constants.HEAT_MAX)
            return Constants.HEAT_MAX;

        return heatLevel;
    }

    /**
     * Returns a heat level that measures how hot (i.e. how large) the lineCount is.
     * The function scales exponentially to guarantee the following:
     * 1 point of heat for 98 lines or fewer (sorry it couldn't be 100 exactly; I'm bad at math).
     * 10 heat (or heat max) for 550 lines or more.
     */
    private static int computeLineCountHeat(long lineCount)
    {
        if (lineCount < 0)
            throw new IllegalArgumentException("computeLineCountHeat() refuses to handle negative line count input `" + lineCount + "`");

        //Use exponential function to calculate heat
        final int LINES_FOR_MAX_HEAT = 550;
        double fixedPart = Math.pow(Constants.HEAT_MAX, 1.0 / (LINES_FOR_MAX_HEAT - 1));
        int heatLevel = (int)((1.0 / fixedPart) * Math.pow(fixedPart, lineCount));

        //If the result is too large, constrain it
        if (heatLevel > Constants.HEAT_MAX)
            return Constants.HEAT_MAX;

        return heatLevel;
    }



    private static void assignHeatLevelsNumberOfCommits(Codebase codebase)
    {
        LOG.info("Calculating heat based on number of commits...");
        final int REQUIRED_NUM_COMMITS_WITHOUT_CHANGING = 5; //the number of consecutive commits where the file is not modified in order to reduce the accumulated heat level.
        final int COMMIT_HEAT_CONSEQUENCE = 2; //how much the heat increases when the file is modified
        final int COMMIT_ABSENCE_HEAT_CONSEQUENCE = -1; //how much the heat decreases if the file is not modified for enough consecutive commits

        Set<FileObject> fileObjectSet = codebase.getActiveFileObjects();
        for (FileObject fileObject : fileObjectSet)
        {
            //The oldest commits are at the front of the LinkedHashMap
            LinkedHashMap<String, HeatObject> commitHashToHeatObjectMap = fileObject.getCommitHashToHeatObjectMap();

            HeatObject lastHeatObject = null;
            int numberOfConsecutiveCommitsWithNoModify = 0;

            for (Map.Entry<String, HeatObject> commitToHeatObjectEntry : commitHashToHeatObjectMap.entrySet())
            {
                HeatObject newerHeatObject = commitToHeatObjectEntry.getValue();
                if (lastHeatObject != null)
                {
                    newerHeatObject.setNumberOfCommitsHeat(lastHeatObject.getNumberOfCommitsHeat()); //use previous heat, then modify

                    //If the file was committed to, incur heat
                    if (newerHeatObject.getNumberOfCommits() > lastHeatObject.getNumberOfCommits())
                    {
                        newerHeatObject.setNumberOfCommitsHeat(newerHeatObject.getNumberOfCommitsHeat() + COMMIT_HEAT_CONSEQUENCE);
                        numberOfConsecutiveCommitsWithNoModify = 0;
                    }
                    //File was not touched in the commit ↓
                    else
                    {
                        numberOfConsecutiveCommitsWithNoModify++;

                        //If file went unchanged for long enough, the heat improved
                        if (numberOfConsecutiveCommitsWithNoModify >= REQUIRED_NUM_COMMITS_WITHOUT_CHANGING)
                        {
                            newerHeatObject.setNumberOfCommitsHeat(newerHeatObject.getNumberOfCommitsHeat() + COMMIT_ABSENCE_HEAT_CONSEQUENCE);
                            numberOfConsecutiveCommitsWithNoModify = 0;
                        }
                    }
                }
                else
                {
                    newerHeatObject.setNumberOfCommitsHeat(Constants.HEAT_MIN); //No commits to the file yet
                }

                lastHeatObject = newerHeatObject;
            }
        }
        LOG.info("Finished calculating heat based on number of commits.");
    }



    private static void assignHeatLevelsNumberOfAuthors(Codebase codebase)
    {
        /*
        ---- General description of this method ----
         By default, every author implicitly has 0 "points" to represent how active they are.
         Every time an author joins, they are given 10 points.
         (So, an author who just pushes 1 commit will be considered absent from the file after 10 commits)
         For every commit after that, they incur 1 point.
         At the same time, every other author loses 1 point because they did not touch the file.
         Once the author's score reaches 0, they are considered "absent" from the file.
         An absent author can re-gain the 10-point penalty upon rejoining.

         The more a person modifies a file, the more they "own" the file...so it takes longer for them to be considered inactive.
         */

        LOG.info("Calculating heat based on number of authors...");
        final int SCORE_PENALTY_FOR_NEW_AUTHOR = 10; //how many consecutive commits another author must make to a file before a particular author can be considered absent
        final int SCORE_PENALTY_FOR_RETURNING = 1; //how many points an author is given when they return

        //Determine total number of authors
        Set<FileObject> fileObjectSet = codebase.getActiveFileObjects();
        final int totalAuthorCount = countTotalNumberOfAuthors(codebase);

        //Assign heat level to every HeatObject based on number of authors
        for (FileObject fileObject : fileObjectSet)
        {
            HashMap<String, Integer> activeAuthors = new HashMap<>(); //the emails of which authors have been committing to the file recently
            //...and their integer score, which increases based on how many commits they have pushed recently

            //The oldest commits are at the front of the LinkedHashMap
            LinkedHashMap<String, HeatObject> commitHashToHeatObjectMap = fileObject.getCommitHashToHeatObjectMap();
            HeatObject lastHeatObject = null;
            int numberOfActiveAuthors = 0;

            //Look at every commit that the file changed in
            for (Map.Entry<String, HeatObject> commitToHeatObjectEntry : commitHashToHeatObjectMap.entrySet())
            {
                HeatObject newerHeatObject = commitToHeatObjectEntry.getValue();

                //Get the author of the commit
                String commitHash = commitToHeatObjectEntry.getKey();
                String authorEmail = codebase.getCommitFromCommitHash(commitHash).getAuthorEmail();

                //Reuse previous heat value for every HeatObject except the first
                if (lastHeatObject != null)
                {
                    //Ensure the file was a part of the commit
                    if (newerHeatObject.getNumberOfCommits() > lastHeatObject.getNumberOfCommits())
                    {
                        //Returning author
                        if (activeAuthors.containsKey(authorEmail) && activeAuthors.get(authorEmail) > 0)
                        {
                            //Add the following: 1 to mark this current commit
                            //..and 1 to reverse the subtraction step below that affects all authors, including this one.
                            int score = activeAuthors.get(authorEmail) + 1 + SCORE_PENALTY_FOR_RETURNING;
                            activeAuthors.put(authorEmail, score);
                        }
                        //New author -> incur a hefty penalty
                        else
                        {
                            //Add the following: REQUIRED_NUM_COMMITS_WITH_AUTHOR_ABSENCE to mark this current commit
                            //..and 1 to reverse the subtraction step below that affects all authors, including this one.
                            int score = SCORE_PENALTY_FOR_NEW_AUTHOR + 1;
                            activeAuthors.put(authorEmail, score);
                            numberOfActiveAuthors++;
                        }

                        for (Map.Entry<String, Integer> authorEntry : activeAuthors.entrySet())
                        {
                            //Decrement the score of every author to indicate that they have not modified the file in this commit
                            int score = authorEntry.getValue();
                            if (score > 0) //if author is active
                            {
                                score--;

                                //If the value is now 0 (the author is sufficiently inactive) and reduce the heat
                                if (score == 0) {
                                    numberOfActiveAuthors--;
                                }
                            }

                            activeAuthors.put(authorEntry.getKey(), score);
                        }
                    }
                }
                //Account for the first commit
                else if (newerHeatObject.getNumberOfCommits() == 1) {
                    activeAuthors.put(authorEmail, SCORE_PENALTY_FOR_NEW_AUTHOR);
                    numberOfActiveAuthors = 1; //there is 1 active author on the first commit
                }


                //Store the new heat level
                //lastHeatBeforeTransformation = heatLevel;
                int heatLevel = activeAuthorsToHeatLevel(numberOfActiveAuthors, totalAuthorCount);
                newerHeatObject.setNumberOfAuthorsHeat(heatLevel);
                lastHeatObject = newerHeatObject;
            }
        }
        LOG.info("Finished calculating heat based on number of authors.");
    }


    /**
     * Transforms numberOfActiveAuthors into a heat level.
     * Currently, this scales linearly based on how close numberOfActiveAuthors is to 4.
     * That is, 4 authors is too many and yields max heat.
     */
    private static int activeAuthorsToHeatLevel(int numberOfActiveAuthors, int totalAuthorCount)
    {
        //Special case for only 1 author in the codebase:
        if (totalAuthorCount == 1)
            return 1; //every file should be the same heat for them

        //Heat should grow exponentially as the numberOfActiveAuthors approaches totalAuthorCount
        /*final int n = totalAuthorCount;
        double base = Math.pow(Constants.HEAT_MAX, 1.0 / (n - 1));
        return (int)((1.0 / base) * Math.pow(base, numberOfActiveAuthors));*/

        final double NUM_AUTHORS_FOR_MAX_HEAT = 4.0; //if a file has this many authors or more, it should have max heat

        if (numberOfActiveAuthors <= 1)
            return 1;
        else if (numberOfActiveAuthors >= NUM_AUTHORS_FOR_MAX_HEAT)
            return Constants.HEAT_MAX;

        return (int)((numberOfActiveAuthors / NUM_AUTHORS_FOR_MAX_HEAT) * Constants.HEAT_MAX);
    }

    public static int countTotalNumberOfAuthors(Codebase codebase)
    {
        Set<FileObject> fileObjectSet = codebase.getActiveFileObjects();
        Set<String> allAuthorSet = new LinkedHashSet<>();
        for (FileObject fileObject : fileObjectSet)
        {
            allAuthorSet.addAll(fileObject.getUniqueAuthors());
        }
        return allAuthorSet.size();
    }


    private static void assignHeatLevelsOverall(Codebase codebase)
    {
        LOG.info("Calculating overall heat...");
        /**
         * Create a map that records the sum of all heat levels from every metric.
         * After every call to an assignHeatLevels method, we must call sumHeatLevels(...) to record
         * the latest heat levels in this map.
         */

        assignHeatLevelsFileSize(codebase);

        assignHeatLevelsNumberOfCommits(codebase);

        assignHeatLevelsNumberOfAuthors(codebase);

        assignHeatLevelsRelativeToAverage(codebase, HeatMetricOptionsExceptOverall.DEGREE_OF_COUPLING);

        assignHeatLevelsRelativeToAverage(codebase, HeatMetricOptionsExceptOverall.BUILD_FAILURE_SCORE);

        assignHeatLevelsRelativeToAverage(codebase, HeatMetricOptionsExceptOverall.CYCLOMATIC_COMPLEXITY);

        assignHeatLevelsRelativeToAverage(codebase, HeatMetricOptionsExceptOverall.CODE_SMELL_SCORE);

        //Add more metrics here if more are needed in the future...

        //Compute and store overall heat
        Set<FileObject> fileObjectSet = codebase.getActiveFileObjects();
        for (FileObject fileObject : fileObjectSet) {
            LinkedHashMap<String, HeatObject> commitHashToHeatObjectMap = fileObject.getCommitHashToHeatObjectMap();
            commitHashToHeatObjectMap.forEach(HeatCalculationUtility::accept);
        }
        LOG.info("Finished calculating overall heat.");
    }


    public static void assignHeatLevels(Codebase codebase)
    {
        assignHeatLevelsOverall(codebase);
    }

    private static void accept(String key, HeatObject heatObject) {
        //Calculate weighted sum of heat
        heatObject.setOverallHeat(
                (
                    (heatObject.getFileSizeHeat() * WEIGHT_FILE_SIZE) +
                    (heatObject.getNumberOfCommitsHeat() * WEIGHT_NUM_OF_COMMITS) +
                    (heatObject.getNumberOfAuthorsHeat() * WEIGHT_NUM_OF_AUTHORS) +
                    (heatObject.getDegreeOfCouplingHeat() * WEIGHT_DEGREE_OF_COUPLING) +
                    (heatObject.getBuildFailureScoreHeat() * WEIGHT_BUILD_FAILURE_SCORE) +
                    (heatObject.getCyclomaticComplexityHeat() * WEIGHT_CYCLOMATIC_COMPLEXITY) +
                    (heatObject.getCodeSmellScoreHeat() * WEIGHT_CODE_SMELL_SCORE)
                ) / (double) HEAT_WEIGHT_TOTAL
        );
    }
}
