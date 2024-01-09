// package intellij_extension.utility;

// import com.insightservice.springboot.utility.RepositoryAnalyzer;

// import org.eclipse.jgit.api.errors.GitAPIException;

// import org.junit.jupiter.api.Test;

// import java.io.Console;
// import java.io.IOException;

// import static org.junit.jupiter.api.Assertions.*;

// public class TestRepositoryAnaylsis {
// @Test
// public void testCheckout() throws GitAPIException, IOException {
// String remoteUrl = "https://github.iu.edu/P532-OOSD/f22-week5-team2.git";
// String branchName = "main";
// String branchName2 = "new_branch";

// RepositoryAnalyzer repositoryAnalyzer = new RepositoryAnalyzer(remoteUrl +
// branchName);
// assertTrue(repositoryAnalyzer.checkout(branchName2));
// assertFalse(repositoryAnalyzer.checkout("fail"));
// repositoryAnalyzer.cleanup();
// }
// @Test
// public void testMergeTwoBranch() throws IOException, GitAPIException{
// String remoteUrl = "https://github.iu.edu/P532-OOSD/f22-week5-team2.git";
// String branchName = "main";
// String branchName2 = "new_branch";
// RepositoryAnalyzer repositoryAnalyzer = new RepositoryAnalyzer(remoteUrl +
// branchName);
// assertTrue(repositoryAnalyzer.mergeTwoBranch(branchName,branchName2));
// assertFalse(repositoryAnalyzer.mergeTwoBranch("fail", "ahhhhhhhh"));
// repositoryAnalyzer.cleanup();
// }

// @Test
// public void testHardReset() throws IOException, GitAPIException{
// String remoteUrl = "https://github.iu.edu/P532-OOSD/f22-week5-team2.git";
// String branchName = "main";
// RepositoryAnalyzer repositoryAnalyzer = new RepositoryAnalyzer(remoteUrl +
// branchName);
// repositoryAnalyzer.hardReset(branchName);
// }

// }
