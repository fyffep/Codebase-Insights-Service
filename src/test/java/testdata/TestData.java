package testdata;

import java.io.File;

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
}
