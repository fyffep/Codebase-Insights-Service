package com.insightservice.springboot.service;

import com.insightservice.springboot.model.knowledge.KnowledgeGraph;
import com.insightservice.springboot.utility.RepositoryAnalyzer;
import com.insightservice.springboot.utility.commit_history.JGitHelper;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.stereotype.Service;

import java.io.IOException;


@Service
public class KnowledgeGraphService
{
    public KnowledgeGraph getKnowledgeGraph(String remoteUrl, String branchName, String oauthToken) throws GitAPIException, IOException
    {
        RepositoryAnalyzer repositoryAnalyzer = null;
        try
        {
            JGitHelper.cloneOrUpdateRepository(remoteUrl, branchName, oauthToken);
            repositoryAnalyzer = new RepositoryAnalyzer(remoteUrl);

            return repositoryAnalyzer.getKnowledgeGraph();
        }
        finally
        {
            //Close the .git files
            if (repositoryAnalyzer != null)
                repositoryAnalyzer.cleanup();
        }
    }
}
