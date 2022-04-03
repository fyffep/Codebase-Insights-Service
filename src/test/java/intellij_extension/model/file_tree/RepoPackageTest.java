package intellij_extension.model.file_tree;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insightservice.springboot.model.codebase.FileObject;
import com.insightservice.springboot.model.file_tree.RepoPackage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import configuration.ObjectMapperConfiguration;

import java.io.File;

import static org.junit.Assert.assertEquals;


/**
 * UNIT TESTING
 * Uses JSON serialization to check if the RepoPackage model can be
 * arranged as expected.
 */
@RunWith(SpringRunner.class)
@Import(ObjectMapperConfiguration.class)
public class RepoPackageTest
{
    @Autowired
    ObjectMapper objectMapper;

    //Ensure a RepoPackage can be created with no difficulties
    @Test
    public void addFileTreeNode_JsonStructure_Mock() throws JsonProcessingException {
        RepoPackage rootPackage = new RepoPackage(new File(".").toPath());
        rootPackage.addFileTreeNode(new FileObject(new File("alpha.java").toPath()));

        RepoPackage subPackage = new RepoPackage(new File("src").toPath());
        subPackage.addFileTreeNode(new FileObject(new File("beta.java").toPath()));
        rootPackage.addFileTreeNode(subPackage);

        String EXPECTED_JSON = "{\"path\":\".\",\"fileTreeNodeList\":[{\"path\":\"alpha.java\",\"filename\":\"alpha.java\",\"degreeOfCouplingHeat\":0,\"latestHeatObject\":null,\"uniqueAuthors\":[],\"uniqueAuthorEmails\":[]},{\"path\":\"src\",\"fileTreeNodeList\":[{\"path\":\"beta.java\",\"filename\":\"beta.java\",\"degreeOfCouplingHeat\":0,\"latestHeatObject\":null,\"uniqueAuthors\":[],\"uniqueAuthorEmails\":[]}]}]}";
        String actualJson = objectMapper.writeValueAsString(rootPackage);

        assertEquals(EXPECTED_JSON, actualJson);
    }
}
