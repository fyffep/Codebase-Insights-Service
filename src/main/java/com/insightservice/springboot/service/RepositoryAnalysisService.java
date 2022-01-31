package com.insightservice.springboot.service;

import com.insightservice.springboot.model.codebase.Codebase;
import com.insightservice.springboot.utility.RepositoryAnalyzer;
import com.insightservice.springboot.utility.commit_history.JGitHelper;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static com.insightservice.springboot.Constants.LOG;

@Service
public class RepositoryAnalysisService
{
    //region Vars
    private static RepositoryAnalysisService instance;
    //endregion

    //region private Constructor
    private RepositoryAnalysisService() {
        //empty
    }
    //endregion

    public void cloneRemoteRepository(String remoteUrl) throws GitAPIException, IOException
    {
        JGitHelper.cloneRepository(remoteUrl);
    }


    /**
     * Clones a GitHub repos into local memory, then analyzes it.
     * All data is placed into a Codebase.
     * Finally, the local repos is deleted from the file system.
     * @param remoteUrl the URL to the home page of a user's GitHub repository
     * @return the Codebase containing all heat and file data.
     */
    public static Codebase extractData(String remoteUrl) throws GitAPIException, IOException
    {
        //Obtain file metrics by analyzing the code base
        RepositoryAnalyzer repositoryAnalyzer = null;
        try
        {
            Codebase codebase = new Codebase();

            //Calculate file sizes for every commit
            repositoryAnalyzer = new RepositoryAnalyzer(remoteUrl);
            RepositoryAnalyzer.attachBranchNameList(codebase);
            codebase.selectDefaultBranch();
            RepositoryAnalyzer.attachCodebaseData(codebase);

            //Now the Codebase contains all the data it needs
            LOG.info("Heat calculations complete. Number of files: " + codebase.getActiveFileObjects().size());

            return codebase;
        }
        catch (IOException | GitAPIException e) {
            //FIXME this is possibly repetitive logging
            e.printStackTrace();
            LOG.error(e.toString());
            LOG.error(e.getMessage());

            throw e;
        }
        finally
        {
            //Enable deletion of local repo
            if (repositoryAnalyzer != null)
                repositoryAnalyzer.cleanup();
            //Delete repos from local storage
            JGitHelper.removeClonedRepository(remoteUrl);
        }
    }


    //TODO UPDATE OR REMOVE THESE FUNCTIONS LEFT BEHIND FROM LAST SEMESTER
    //region View-to-Model communication bridge
//    public void heatMapComponentSelected(String id) {
//        codeBase.heatMapComponentSelected(id);
//    }
//
//    // A way for FileHistoryDetails to get the branch list.
//    public void branchListRequested() {
//        codeBase.branchListRequested();
//    }
//
//    public void newBranchSelected(String branchName) {
//        codeBase.newBranchSelected(branchName);
//    }

//    public void newHeatMetricSelected(String heatMetricOption) {
//        Constants.HeatMetricOptions newOption;
//        if (heatMetricOption.equals(HEAT_METRIC_OPTIONS.get(0))) {
//            newOption = HeatMetricOptions.OVERALL;
//        } else if (heatMetricOption.equals(HEAT_METRIC_OPTIONS.get(1))) {
//            newOption = HeatMetricOptions.FILE_SIZE;
//        } else if (heatMetricOption.equals(HEAT_METRIC_OPTIONS.get(2))) {
//            newOption = HeatMetricOptions.NUM_OF_COMMITS;
//        } else {
//            newOption = HeatMetricOptions.NUM_OF_AUTHORS;
//        }
//
//        codeBase.newHeatMetricSelected(newOption);
//    }
//
//    public void commitSelected(String commitHash) {
//        codeBase.commitSelected(commitHash);
//    }
//
//    public void changeHeatMapToCommit(String commitHash) {
//        codeBase.changeHeatMapToCommit(commitHash);
//    }
//
//    public void heatMapGroupingChanged(String newTab) {
//        GroupingMode newGroupingMode;
//        if (newTab.equals(Constants.COMMIT_GROUPING_TEXT)) {
//            newGroupingMode = GroupingMode.COMMITS;
//        } else {
//            newGroupingMode = GroupingMode.PACKAGES;
//        }
//
//        codeBase.heatMapGroupingChanged(newGroupingMode);
//    }
    //endregion
}
