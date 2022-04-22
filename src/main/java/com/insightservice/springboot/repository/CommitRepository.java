package com.insightservice.springboot.repository;

import com.insightservice.springboot.model.codebase.Commit;
import com.insightservice.springboot.model.codebase.FileObject;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.LinkedHashSet;

public interface CommitRepository extends MongoRepository<Commit, String>
{
    @Query("{ 'gitHubUrl' : ?0 }")
    LinkedHashSet<Commit> findByGitHubUrl(String gitHubUrl);
}