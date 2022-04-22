package testdata;

import com.insightservice.springboot.model.codebase.Codebase;
import com.insightservice.springboot.model.codebase.Commit;
import com.insightservice.springboot.model.codebase.FileObject;
import com.insightservice.springboot.model.codebase.HeatObject;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.File;
import java.nio.file.Path;

public class TestData
{
    public static final String VALID_REMOTE_URL = "https://github.com/fyffep/codebase-insights-intellij"; //you must edit CLONED_REPO_PATH if changing this
    public static final String BOGUS_REMOTE_URL = "https://gitfake.com/testdouble/java-testing-example"; //notice the word "gitfake"
    public static final String NOT_A_REMOTE_URL = "https://localhost/";
    public static final String SNEAKY_REMOTE_URL = "https://github.com/fyffep/codebase-insights-intelligence"; //real user, fake repo
    public static final String PRIVATE_REMOTE_URL = "https://github.com/fyffep/deployment-codebase-insights-service";
    public static final String MAIN_ONLY_BRANCH_REMOTE_URL = "https://github.com/fyffep/MainBranchOnlyRepo";
    public static final File CLONED_REPO_PATH = new File("repositories/codebase-insights-intellij"); //where we expect the repo with REMOTE_URL to be cloned to
    public static final String MASTER_BRANCH = "master";
    public static final String BOGUS_BRANCH = "cmjljksdcl";





    //MOCK CODEBASE - All files appear at all commits here.
    //This data should be reset (re-created) every time setupMockCodebase() is called.
    public static final String HASH_1 = "hash1";
    public static final String FILE_PATH_A = "fileA";
    public static final String FILE_PATH_B = "fileB";
    public static final String FILE_PATH_C = "fileC";

    public static Codebase mockCodebase;
    public static FileObject fileA;
    public static FileObject fileB;
    public static FileObject fileC;
    public static HeatObject heatObject1A;
    public static HeatObject heatObject1B;
    public static HeatObject heatObject1C;

    public static void setupMockCodebase()
    {
        mockCodebase = new Codebase();
        mockCodebase.setLatestCommitHash(HASH_1);

        //Note that no Commit objects are created.

        //Create file objects
        fileA = new FileObject(Path.of(FILE_PATH_A));
        fileB = new FileObject(Path.of(FILE_PATH_B));
        fileC = new FileObject(Path.of(FILE_PATH_C));
        mockCodebase.getActiveFileObjects().add(fileA);
        mockCodebase.getActiveFileObjects().add(fileB);
        mockCodebase.getActiveFileObjects().add(fileC);

        //Create HeatObjects for commit HASH_1
        heatObject1A = new HeatObject();
        heatObject1A.setFileSize(100);
        heatObject1A.setLineCount(70);
        heatObject1A.setBuildFailureScore(3);
        heatObject1A.setCyclomaticComplexity(15);
        fileA.setHeatForCommit(HASH_1, heatObject1A);

        heatObject1B = new HeatObject();
        heatObject1B.setFileSize(300);
        heatObject1B.setLineCount(200);
        heatObject1B.setBuildFailureScore(0);
        heatObject1B.setCyclomaticComplexity(6);
        fileB.setHeatForCommit(HASH_1, heatObject1B);

        heatObject1C = new HeatObject();
        heatObject1C.setFileSize(200);
        heatObject1C.setLineCount(30);
        heatObject1C.setBuildFailureScore(2);
        heatObject1C.setCyclomaticComplexity(4);
        fileC.setHeatForCommit(HASH_1, heatObject1C);

        //more HeatObjects at the other commits can go here...
    }
}
