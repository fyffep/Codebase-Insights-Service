package com.insightservice.springboot.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.eclipse.jgit.diff.DiffEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.insightservice.springboot.model.branchHealthComparision.TwoBranches;
import com.insightservice.springboot.model.branchHealthComparision.TwoRepoPackage;
import com.insightservice.springboot.model.codebase.Codebase;
import com.insightservice.springboot.model.file_tree.RepoPackage;
import com.insightservice.springboot.payload.SettingsPayload;
import com.insightservice.springboot.utility.FileTreeCreator;
import com.insightservice.springboot.utility.GroupFileObjectUtility;
import com.insightservice.springboot.utility.RepositoryAnalyzer;
import static com.insightservice.springboot.Constants.LOG;

@Service
public class HealthComparisionService {
    /*
     * We are in essence calculating for a branch before and after merge ,therefore
     * e are importing RepositoryAnalysisServi
     * e
     */

    public List<RepoPackage> getHealthDifference(TwoBranches twoBranches, String remoteURL, Codebase codebaseParent,
            Codebase codebaseChild) {

        String parentBranch = twoBranches.parentBranch;
        String childBranch = twoBranches.childBranch;

        try {
            RepositoryAnalyzer repositoryAnalyzer = new RepositoryAnalyzer(remoteURL + twoBranches.parentBranch);
            if (repositoryAnalyzer.mergeTwoBranch(parentBranch, childBranch)) {
                LOG.info("Succesfully merged two branches...");
                // List<DiffEntry> diffentry = repositoryAnalyzer.ShowBranchDiff(parentBranch,
                // childBranch);
                RepoPackage fileTreeParent = FileTreeCreator.createFileTree(
                        codebaseParent.getActiveFileObjectsExcludeDeletedFiles(codebaseParent.getLatestCommitHash()));
                RepoPackage fileTreeChild = FileTreeCreator.createFileTree(
                        codebaseChild.getActiveFileObjectsExcludeDeletedFiles(codebaseChild.getLatestCommitHash()));

                // RepoPackage fileTreeMerged =
                // FileTreeCreator.createFileTree(codebaseMerged.getActiveFileObjectsExcludeDeletedFiles(codebaseMerged.getLatestCommitHash()));
                // repositoryAnalyzer.hardReset(parentBranch);

                LOG.info("Returning the two fileTrees");

                List<RepoPackage> repoPackages = new ArrayList<>();
                // repoPackages.add(fileTreeMerged);
                repoPackages.add(fileTreeChild);
                repoPackages.add(fileTreeParent);
                return repoPackages;

            }
        } catch (Exception e) {
            LOG.info("Cannot retirieve file from local datastorage " + remoteURL + twoBranches.parentBranch);
            return new ArrayList<>();
        }

        // TODO check if parent branch is a parent of child branch

        LOG.info("Returning empty Array: Something is wrong!");

        return new ArrayList<>();
    }
}
