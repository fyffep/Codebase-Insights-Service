package intellij_extension.utility.commit_history;

import com.insightservice.springboot.exception.BadUrlException;
import com.insightservice.springboot.utility.RepositoryAnalyzer;
import com.insightservice.springboot.utility.commit_history.JGitHelper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static testdata.TestData.BOGUS_REMOTE_URL;

public class JGitHelperTest
{
    @Test
    void constructor_RemoteUrlParameter_ThrowsBadUrlException() {
        assertThrows(BadUrlException.class, () -> {
            JGitHelper.cloneRepository(BOGUS_REMOTE_URL, "master");
            new RepositoryAnalyzer(BOGUS_REMOTE_URL);
        });
    }
}
