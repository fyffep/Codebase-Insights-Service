package com.insightservice.springboot.utility.commit_history;

import com.insightservice.springboot.exception.BadUrlException;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Arrays;

import static com.insightservice.springboot.Constants.LOG;
import static com.insightservice.springboot.Constants.REPO_STORAGE_DIR;

/**
 * Contains utility methods for opening Git repositories.
 * Credit to the JGit Cookbook for creating this class https://github.com/centic9/jgit-cookbook/tree/master/src/main/java/org/dstadler/jgit/helper
 */
public class JGitHelper
{
    private JGitHelper() {
        //This is a utility class
    }

    private static String getRepositoryNameFromUrl(String remoteUrl) throws MalformedURLException
    {
        //Determine name of new directory
        String[] repoNameArr = remoteUrl.strip().split("/");
        if (repoNameArr.length < 3) //at least 2 slashes are in a Git URL
        {
            throw new MalformedURLException(remoteUrl + " is not a valid repository URL.");
        }
        String repoName = repoNameArr[repoNameArr.length - 1];
        //TODO trim ".git" from URL
        return repoName;
    }

    public static File getPathOfLocalRepository(String remoteUrl) throws MalformedURLException
    {
        String repoName = getRepositoryNameFromUrl(remoteUrl);
        return new File(REPO_STORAGE_DIR + File.separator + repoName);
    }

    public static File cloneRepository(String remoteUrl, String branchName) throws GitAPIException, IOException
    {
        //Make an empty dir for the cloned repo
        File directory = getPathOfLocalRepository(remoteUrl);
        if (directory.exists())
            FileUtils.deleteDirectory(directory);
        directory.mkdirs();

        //Clone
        LOG.info("Cloning from " + remoteUrl + " to " + directory);
        try (Git result = Git.cloneRepository()
                .setBranch(branchName) //Note to self: this line can be omitted to clone default branch
                .setURI(remoteUrl)
                .setDirectory(directory)
                .call()) {
        }
        catch (Exception ex)
        {
            throw new BadUrlException("No repository could be read from your GitHub URL.");
        }

        return directory;
    }

    public static void removeClonedRepository(String remoteUrl) throws IOException
    {
        String repoName = getRepositoryNameFromUrl(remoteUrl);
        //Remove the cloned repo
        FileUtils.deleteDirectory(getPathOfLocalRepository(remoteUrl));
        LOG.info("Removed the repository named `"+repoName+"` from the file system.");
    }

    public static Repository openLocalRepository(File projectPath) throws IOException
    {
        try
        {
            FileRepositoryBuilder builder = new FileRepositoryBuilder();
            return builder
                    .readEnvironment() // scan environment GIT_* variables
                    .findGitDir(projectPath)
                    .build();
        }
        catch (IllegalArgumentException ex)
        {
            throw new FileNotFoundException("openLocalRepository(...) failed. Perhaps the repository was not cloned yet. "
                    + ex.toString());
        }
    }

    public static Repository openLocalRepository(String remoteUrl) throws IOException
    {
        File pathToRepository = getPathOfLocalRepository(remoteUrl);
        assert pathToRepository.exists();

        return openLocalRepository(pathToRepository);
    }












    //UNUSED
    /*public static Repository openLocalRepository() throws IOException
    {
        final String projectRootPath = locateProjectRoot();
        assert projectRootPath != null;

        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        return builder
                .readEnvironment() // scan environment GIT_* variables
                //.findGitDir() // scan up the file system tree
                .findGitDir(new File(projectRootPath))
                .build();
    }*/

    //UNUSED
    /**
     * @return the path of the project that the user has open in IntelliJ or null
     * as a default.
     */
    /*public static String locateProjectRoot()
    {
        //Pull the 'project' from CodebaseInsightsToolWindowFactory, and wait until it exists if necessary
        synchronized (CodebaseInsightsToolWindowFactory.projectSynchronizer) {
            if (CodebaseInsightsToolWindowFactory.getProject() == null) {
                try {
                    //Wait until the 'project' is not-null
                    CodebaseInsightsToolWindowFactory.projectSynchronizer.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();

                    //Restore interrupted state... (recommended by SonarQube)
                    Thread.currentThread().interrupt();
                }
            }
        }
        return CodebaseInsightsToolWindowFactory.getProject().getBasePath();
    }*/
}
