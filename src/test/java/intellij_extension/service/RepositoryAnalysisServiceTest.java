package intellij_extension.service;

import com.insightservice.springboot.Constants;
import com.insightservice.springboot.Main;
import com.insightservice.springboot.exception.BadBranchException;
import com.insightservice.springboot.exception.BadUrlException;
import com.insightservice.springboot.repository.CodebaseRepository;
import com.insightservice.springboot.repository.CommitRepository;
import com.insightservice.springboot.repository.FileObjectRepository;
import com.insightservice.springboot.service.RepositoryAnalysisService;
import intellij_extension.model.file_tree.RepositoryAnalysisServiceConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.*;
import static testdata.TestData.*;

/**
 * INTEGRATION TESTING
 */
//
//@DataMongoTest
//@AutoConfigureDataMongo
//@SpringBootTest(classes = CodebaseRepository.class)
//
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(classes = Main.class)
//@SpringBootTest
//@AutoConfigureMockMvc
//
@SpringBootTest
@RunWith(SpringRunner.class)
@Import(RepositoryAnalysisServiceConfiguration.class)
@ContextConfiguration
public class RepositoryAnalysisServiceTest
{
    @Autowired
    RepositoryAnalysisService repositoryAnalysisService;

    @Autowired
    CodebaseRepository codebaseRepository;
    @Autowired
    FileObjectRepository fileObjectRepository;
    @Autowired
    CommitRepository commitRepository;

    //region cloneRemoteRepository tests
    @Test
    public void cloneRemoteRepository_ValidUrl_ValidBranch()
    {
        assertDoesNotThrow(() ->{
            repositoryAnalysisService.cloneRemoteRepository(VALID_REMOTE_URL, MASTER_BRANCH);
        });
    }

    @Test
    public void cloneRemoteRepository_ValidUrl_BogusBranch()
    {
        assertThrows(BadBranchException.class, () -> {
            repositoryAnalysisService.cloneRemoteRepository(VALID_REMOTE_URL, BOGUS_BRANCH);
        });
    }

    @Test
    public void cloneRemoteRepository_PrivateUrl_ValidBranch()
    {
        assertThrows(BadUrlException.class, () -> {
            repositoryAnalysisService.cloneRemoteRepository(PRIVATE_REMOTE_URL, BOGUS_BRANCH);
        });
    }
    //endregion



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