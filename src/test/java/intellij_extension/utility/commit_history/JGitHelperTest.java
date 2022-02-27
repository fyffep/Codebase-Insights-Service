package intellij_extension.utility.commit_history;

import com.insightservice.springboot.exception.BadUrlException;
import com.insightservice.springboot.model.codebase.Codebase;
import com.insightservice.springboot.utility.RepositoryAnalyzer;
import com.insightservice.springboot.utility.commit_history.JGitHelper;
import org.apache.tomcat.util.http.fileupload.FileUtils;
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
        JGitHelper.cloneRepository(VALID_REMOTE_URL, MASTER_BRANCH); //method being tested

        //Ensure it exists locally
        assertTrue(CLONED_REPO_PATH.exists());
    }

    @Test
    void cloneRepository_BogusRemoteUrlParameter_ThrowsBadUrlException() {
        assertThrows(BadUrlException.class, () -> {
            JGitHelper.cloneRepository(BOGUS_REMOTE_URL, MASTER_BRANCH); //method being tested
            new RepositoryAnalyzer(BOGUS_REMOTE_URL); //triggers the exception
        });
    }

    @Test
    void cloneRepository_SneakyRemoteUrlParameter_ThrowsBadUrlException() {
        assertThrows(BadUrlException.class, () -> {
            JGitHelper.cloneRepository(SNEAKY_REMOTE_URL, MASTER_BRANCH); //method being tested
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

        assertTrue(JGitHelper.checkIfLatestCommitIsUpToDate(codebase)); //method being tested
    }

    @Test
    void checkIfLatestCommitIsUpToDate_OldCommit_MasterBranch() throws GitAPIException {
        Codebase codebase = new Codebase();
        codebase.setGitHubUrl(VALID_REMOTE_URL);
        codebase.setActiveBranch(MASTER_BRANCH);
        codebase.setLatestCommitHash("4c98689dd08627fed0e5e4363efd101d6e4cb1c0"); //some random hash in our commit history

        assertFalse(JGitHelper.checkIfLatestCommitIsUpToDate(codebase)); //method being tested
    }

    @Test
    void checkIfLatestCommitIsUpToDate_LatestCommit_OtherBranch() throws GitAPIException {
        Codebase codebase = new Codebase();
        codebase.setGitHubUrl(VALID_REMOTE_URL);
        codebase.setActiveBranch("ui-development-commit-history");
        codebase.setLatestCommitHash("5c92c6f0818dd2b139cfb1f054c89ef7797dbe09"); //this is the final commit on this dead branch

        assertTrue(JGitHelper.checkIfLatestCommitIsUpToDate(codebase)); //method being tested
    }
}
