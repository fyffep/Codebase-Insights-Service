package com.insightservice.springboot.service;

import java.util.LinkedHashSet;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.stereotype.Service;

import com.insightservice.springboot.model.codebase.Codebase;
import com.insightservice.springboot.utility.RepositoryAnalyzer;

@Service
public class RetrieveAllBranchService {

    public LinkedHashSet<String> getAllBranch(Codebase codebase) {
        try {
            RepositoryAnalyzer.attachBranchNameList(codebase);
            return codebase.getBranchNameList();
        } catch (GitAPIException e) {

        }

        return new LinkedHashSet<>();

    }

}
