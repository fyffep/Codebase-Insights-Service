package com.insightservice.springboot.utility.commit_history;

import com.insightservice.springboot.exception.BadUrlException;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

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

    private static File getPathOfLocalRepository(String remoteUrl) throws MalformedURLException
    {
        String repoName = getRepositoryNameFromUrl(remoteUrl);
        return new File(REPO_STORAGE_DIR + File.separator + repoName);
    }

    public static void cloneRepository(String remoteUrl) throws GitAPIException, IOException
    {
        //Make an empty dir for the cloned repo
        File directory = getPathOfLocalRepository(remoteUrl);
        if (directory.exists())
            FileUtils.deleteDirectory(directory);
        directory.mkdirs();

        //Clone
        System.out.println("Cloning from " + remoteUrl + " to " + directory);
        try (Git result = Git.cloneRepository()
                .setURI(remoteUrl)
                .setDirectory(directory)
                .call()) {
        }
        catch (Exception ex)
        {
            throw new BadUrlException("No repository could be read from your GitHub URL.");
        }
    }

    public static void removeClonedRepository(String remoteUrl) throws IOException
    {
        String repoName = getRepositoryNameFromUrl(remoteUrl);
        //Remove the cloned repo
        FileUtils.deleteDirectory(getPathOfLocalRepository(remoteUrl));
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
    //Same as above method, but requires a path parameter
    /*public static Repository openLocalRepository(File projectPath) throws IOException
    {
        FileRepositoryBuilder builder = new FileRepositoryBuilder();
        return builder
                .readEnvironment() // scan environment GIT_* variables
                .findGitDir(projectPath)
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
