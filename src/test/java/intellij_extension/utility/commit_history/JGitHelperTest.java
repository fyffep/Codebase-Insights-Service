package intellij_extension.utility.commit_history;

import com.insightservice.springboot.exception.BadUrlException;
import com.insightservice.springboot.model.codebase.Codebase;
import com.insightservice.springboot.utility.RepositoryAnalyzer;
import com.insightservice.springboot.utility.commit_history.JGitHelper;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import static org.junit.jupiter.api.Assertions.*;
import static testdata.TestData.*;

/**
 * UNIT TESTING
 */
public class JGitHelperTest
{
    //Ensure a repo can be cloned to local file system
    @Test
    void cloneRepository_RemoteUrlParameter_DirectoryShouldExist() throws GitAPIException, IOException {
        //Remove existing dir
        if (CLONED_REPO_PATH.exists())
            FileUtils.deleteDirectory(CLONED_REPO_PATH);

        //Clone repo
        String oauthToken = ""; //No token
        JGitHelper.cloneRepository(VALID_REMOTE_URL, MASTER_BRANCH, oauthToken); //method being tested

        //Ensure it exists locally
        assertTrue(CLONED_REPO_PATH.exists());
    }

    @Test
    void cloneRepository_BogusRemoteUrlParameter_ThrowsBadUrlException() {
        assertThrows(BadUrlException.class, () -> {
            String oauthToken = ""; //No token
            JGitHelper.cloneRepository(BOGUS_REMOTE_URL, MASTER_BRANCH, oauthToken); //method being tested
            new RepositoryAnalyzer(BOGUS_REMOTE_URL); //triggers the exception
        });
    }

    @Test
    void cloneRepository_SneakyRemoteUrlParameter_ThrowsBadUrlException() {
        assertThrows(BadUrlException.class, () -> {
            String oauthToken = ""; //No token
            JGitHelper.cloneRepository(SNEAKY_REMOTE_URL, MASTER_BRANCH, oauthToken); //method being tested
            new RepositoryAnalyzer(SNEAKY_REMOTE_URL); //triggers the exception
        });
    }


    //Ensure a local file path (i.e. the location of the cloned repo) can be determined from a URL correctly
    @Test
    void getPathOfLocalRepository_RemoteUrlParameter_TestData() throws MalformedURLException {
        File file = JGitHelper.getPathOfLocalRepository(VALID_REMOTE_URL);  //method being tested

        assertEquals(file.getPath(), CLONED_REPO_PATH.getPath());
    }

    @Test
    void cloneRepository_NotARemoteUrlParameter_ThrowsMalformedURLException() {
        assertThrows(MalformedURLException.class, () -> {
            File file = JGitHelper.getPathOfLocalRepository(NOT_A_REMOTE_URL);  //method being tested
            assertNotNull(file);
        });
    }

    @Test
    void cloneRepository_EmptyUrlParameter_ThrowsMalformedURLException() {
        assertThrows(MalformedURLException.class, () -> {
            File file = JGitHelper.getPathOfLocalRepository("");  //method being tested
            assertNotNull(file);
        });
    }



    @Test
    void checkIfLatestCommitIsUpToDate_LatestCommit_MasterBranch() throws GitAPIException {
        Codebase codebase = new Codebase();
        codebase.setGitHubUrl(VALID_REMOTE_URL);
        codebase.setActiveBranch(MASTER_BRANCH);
        codebase.setLatestCommitHash("c60e6975fb2c60810cd2eedf31bf5075b3d02cd4"); //you should update with the latest commit if this fails

        String oauthToken = ""; //No token
        assertTrue(JGitHelper.checkIfLatestCommitIsUpToDate(codebase, oauthToken)); //method being tested
    }

    @Test
    void checkIfLatestCommitIsUpToDate_OldCommit_MasterBranch() throws GitAPIException {
        Codebase codebase = new Codebase();
        codebase.setGitHubUrl(VALID_REMOTE_URL);
        codebase.setActiveBranch(MASTER_BRANCH);
        codebase.setLatestCommitHash("4c98689dd08627fed0e5e4363efd101d6e4cb1c0"); //some random hash in our commit history

        String oauthToken = ""; //No token
        assertFalse(JGitHelper.checkIfLatestCommitIsUpToDate(codebase, oauthToken)); //method being tested
    }

    @Test
    void checkIfLatestCommitIsUpToDate_LatestCommit_OtherBranch() throws GitAPIException {
        Codebase codebase = new Codebase();
        codebase.setGitHubUrl(VALID_REMOTE_URL);
        codebase.setActiveBranch("ui-development-commit-history");
        codebase.setLatestCommitHash("5c92c6f0818dd2b139cfb1f054c89ef7797dbe09"); //this is the final commit on this dead branch

        String oauthToken = ""; //No token
        assertTrue(JGitHelper.checkIfLatestCommitIsUpToDate(codebase, oauthToken)); //method being tested
    }



    @Test
    void updateLocalRepository_MainBranch() throws IOException, GitAPIException {
        final String BRANCH_UNDER_TEST = "main";
        final String REMOTE_URL = "https://github.com/fyffep/MainBranchOnlyRepo";
        //region testing environment setup
        //Perform a clone of the latest version of the repo
        File directory = JGitHelper.getPathOfLocalRepository(REMOTE_URL);
        if (directory.exists())
            FileUtils.deleteDirectory(directory); //deletion makes each test independent
        directory.mkdirs();
        try (Git result = Git.cloneRepository()
                .setBranch(BRANCH_UNDER_TEST)
                .setURI(REMOTE_URL)
                .setDirectory(directory)
                .call()) {
        }
        //endregion

        JGitHelper.updateLocalRepository(REMOTE_URL); //method under test
        //just make sure it doesn't throw anything

        //region cleanup
        FileUtils.deleteDirectory(directory);
        //endregion
    }
}
