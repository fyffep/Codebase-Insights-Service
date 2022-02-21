package intellij_extension.utility.commit_history;

import com.insightservice.springboot.exception.BadUrlException;
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
}
