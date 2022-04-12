package intellij_extension.utility.ci_analyzer;

import com.insightservice.springboot.utility.commit_history.JGitHelper;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static testdata.TestData.*;
import static testdata.TestData.CLONED_REPO_PATH;

public class GithubActionsAnalyzerTest {

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
}
