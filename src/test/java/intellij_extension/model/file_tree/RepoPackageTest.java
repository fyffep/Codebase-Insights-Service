package intellij_extension.model.file_tree;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.insightservice.springboot.model.codebase.FileObject;
import com.insightservice.springboot.model.file_tree.RepoPackage;
import com.insightservice.springboot.serialize.PathSerializer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;


/**
 * UNIT TESTING
 * Uses JSON serialization to check if the RepoPackage model can be
 * arranged as expected.
 */
@RunWith(SpringRunner.class)
public class RepoPackageTest
{
    //Simulate the ObjectMapper that is used for Spring runtime
    @TestConfiguration
    static class ObjectMapperImplTestContextConfiguration {
        @Bean
        public ObjectMapper objectMapper() {
            ObjectMapper objectMapper = new ObjectMapper();
            SimpleModule module = new SimpleModule();
            module.addSerializer(Path.class, new PathSerializer());
            objectMapper.registerModule(module);

            return objectMapper;
        }
    }
    @Autowired
    ObjectMapper objectMapper;

    //Ensure a RepoPackage can be created with no difficulties
    @Test
    public void addFileTreeNode_Structure_PathSerializer() throws JsonProcessingException {
        RepoPackage rootPackage = new RepoPackage(new File(".").toPath());
        rootPackage.addFileTreeNode(new FileObject(new File("alpha.java").toPath()));

        RepoPackage subPackage = new RepoPackage(new File("src").toPath());
        subPackage.addFileTreeNode(new FileObject(new File("beta.java").toPath()));
        rootPackage.addFileTreeNode(subPackage);

        String EXPECTED_JSON = "{\"path\":\".\",\"fileTreeNodeList\":[{\"path\":\"alpha.java\",\"filename\":\"alpha.java\",\"commitHashToHeatObjectMap\":{},\"uniqueAuthors\":[],\"uniqueAuthorEmails\":[],\"latestCommitInTreeWalk\":\"\",\"latestCommitInDiffEntryList\":\"\"},{\"path\":\"src\",\"fileTreeNodeList\":[{\"path\":\"beta.java\",\"filename\":\"beta.java\",\"commitHashToHeatObjectMap\":{},\"uniqueAuthors\":[],\"uniqueAuthorEmails\":[],\"latestCommitInTreeWalk\":\"\",\"latestCommitInDiffEntryList\":\"\"}]}]}";
        String actualJson = objectMapper.writeValueAsString(rootPackage);

        assertEquals(EXPECTED_JSON, actualJson);
    }
}
