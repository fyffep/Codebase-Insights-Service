package intellij_extension.service;

import com.insightservice.springboot.Constants;
import com.insightservice.springboot.exception.BadBranchException;
import com.insightservice.springboot.exception.BadUrlException;
import com.insightservice.springboot.service.RepositoryAnalysisService;
import intellij_extension.model.file_tree.RepositoryAnalysisServiceConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.*;
import static testdata.TestData.*;

/**
 * INTEGRATION TESTING
 */
@RunWith(SpringRunner.class)
@Import(RepositoryAnalysisServiceConfiguration.class)
public class RepositoryAnalysisServiceTest
{
    @Autowired
    RepositoryAnalysisService repositoryAnalysisService;


    //region extractDataToCodebase tests
    @Test
    public void extractDataToCodebase_ValidUrl_MainBranch()
    {
        //This codebase doesn't have a master branch, but we don't specify a branch to clone
        assertDoesNotThrow(() ->{
            repositoryAnalysisService.getOrCreateCodebase(MAIN_ONLY_BRANCH_REMOTE_URL, Constants.USE_DEFAULT_BRANCH);
        });
    }

    @Test
    public void extractDataToCodebase_ValidUrl_ValidBranch()
    {
        assertDoesNotThrow(() ->{
            repositoryAnalysisService.getOrCreateCodebase(VALID_REMOTE_URL, MASTER_BRANCH);
        });
    }
    //endregion
}