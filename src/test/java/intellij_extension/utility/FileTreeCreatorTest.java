package intellij_extension.utility;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insightservice.springboot.model.codebase.Codebase;
import com.insightservice.springboot.model.codebase.FileObject;
import com.insightservice.springboot.model.file_tree.RepoPackage;
import com.insightservice.springboot.model.file_tree.RepoTreeNode;
import com.insightservice.springboot.service.RepositoryAnalysisService;
import com.insightservice.springboot.utility.FileTreeCreator;
import configuration.ObjectMapperConfiguration;
import intellij_extension.model.file_tree.RepositoryAnalysisServiceConfiguration;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.Assert.*;

@Import({ObjectMapperConfiguration.class, RepositoryAnalysisServiceConfiguration.class})
public class FileTreeCreatorTest
{
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    RepositoryAnalysisService repositoryAnalysisService;

    @Test
    public void createFileTree_FilesAndPackagesExist_Mock()
    {
        //Prepare mock file tree...
        //File 1
        Path myClassPath = Path.of("src/MyClass.java");
        FileObject myClass = new FileObject(myClassPath);
        //File 2
        Path myModelPath = Path.of("src/model/Programmer.java");
        FileObject programmer = new FileObject(myModelPath);
        //Create file tree
        Codebase codebase = new Codebase();
        codebase.getActiveFileObjects().add(myClass);
        codebase.getActiveFileObjects().add(programmer);
        RepoPackage output = FileTreeCreator.createFileTree(codebase.getActiveFileObjects());


        //Check for certain files/packages...
        //Ensure root exists
        assertEquals(".", output.getPath().toString());
        //Ensure src exists
        RepoPackage actualSrc = (RepoPackage) output.getFileTreeNodeList().get(0);
        assertEquals("src", actualSrc.getPath().toString());
        //Ensure MyClass.java exists
        FileObject actualMyClass = (FileObject) actualSrc.getFileTreeNodeList().stream()
                .filter(repoTreeNode -> repoTreeNode.getPath().equals(myClassPath))
                .findAny()
                .orElse(null);
        assertEquals(myClass, actualMyClass);
        //Ensure src/model exists
        RepoPackage actualModel = (RepoPackage) actualSrc.getFileTreeNodeList().stream()
                .filter(repoTreeNode -> repoTreeNode.getPath().toString().equals("model"))
                .findAny()
                .orElse(null);
        assertNotNull(actualModel);
        assertEquals("model", actualModel.getPath().toString());
        //Ensure Programmer.java exists
        RepoTreeNode actualProgrammer = actualModel.getFileTreeNodeList().get(0);
        assertEquals(programmer, actualProgrammer);

        //Check sizes of packages...
        final int ROOT_SIZE = 1;
        assertEquals(ROOT_SIZE, output.getFileTreeNodeList().size());
        final int SRC_SIZE = 2;
        assertEquals(SRC_SIZE, actualSrc.getFileTreeNodeList().size());
        final int MODEL_SIZE = 1;
        assertEquals(MODEL_SIZE, actualModel.getFileTreeNodeList().size());
    }



    @Test
    public void createFileTree_JsonStructure_PatientManagerRepo() throws GitAPIException, IOException
    {
//        //Prepare test data
//        Codebase codebase = new Codebase();
//        JGitHelper.cloneRepository(REMOTE_URL, MASTER_BRANCH);
//        RepositoryAnalyzer repositoryAnalyzer = new RepositoryAnalyzer(REMOTE_URL);
//        repositoryAnalyzer.attachBranchNameList(codebase); //method being tested
//
//        //FIXME there are files here which should not exist
//        String EXPECTED_JSON = "";
//        String actualJson = objectMapper.writeValueAsString(codebase.getActiveFileObjects());
//
//        assertEquals(EXPECTED_JSON, actualJson);
    }
}
