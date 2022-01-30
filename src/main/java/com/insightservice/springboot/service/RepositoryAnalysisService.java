package com.insightservice.springboot.service;

import com.insightservice.springboot.utility.commit_history.JGitHelper;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.stereotype.Service;

import java.io.IOException;


@Service
public class RepositoryAnalysisService
{
    public void cloneMyRepository(String remoteUrl) throws GitAPIException, IOException
    {
        JGitHelper.cloneRepository(remoteUrl);
    }
}
