package intellij_extension.utility;

import com.insightservice.springboot.model.codebase.Codebase;
import com.insightservice.springboot.utility.RepositoryAnalyzer;
import com.insightservice.springboot.utility.commit_history.JGitHelper;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;

import static com.insightservice.springboot.Constants.LOG;

//TODO implement proper Spring Boot Service testing https://www.baeldung.com/spring-boot-testing
public class TempClassForRepoAnalysis
{
    public static void cloneRemoteRepository(String remoteUrl, String branchName) throws GitAPIException, IOException
    {
        JGitHelper.cloneRepository(remoteUrl, branchName);
    }

    public static Codebase extractDataToCodebase(String remoteUrl, String branchName) throws GitAPIException, IOException
    {
        cloneRemoteRepository(remoteUrl, branchName);

        //Obtain file metrics by analyzing the code base
        RepositoryAnalyzer repositoryAnalyzer = null;
        try
        {
            Codebase codebase = new Codebase();

            //Calculate file sizes for every commit
            repositoryAnalyzer = new RepositoryAnalyzer(remoteUrl);
            RepositoryAnalyzer.attachBranchNameList(codebase);
            codebase.selectDefaultBranch();
            RepositoryAnalyzer.attachCodebaseData(codebase);

            //Now the Codebase contains all the data it needs
            LOG.info("Heat calculations complete. Number of files: " + codebase.getActiveFileObjects().size());

            return codebase;
        }
        catch (IOException | GitAPIException e) {
            //FIXME this is possibly repetitive logging
            e.printStackTrace();
            LOG.error(e.toString());
            LOG.error(e.getMessage());

            throw e;
        }
        finally
        {
            //Enable deletion of local repo
            if (repositoryAnalyzer != null)
                repositoryAnalyzer.cleanup();
            //Delete repos from local storage
            JGitHelper.removeClonedRepository(remoteUrl);
        }
    }
}
