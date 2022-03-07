package com.insightservice.springboot.repository;

import com.insightservice.springboot.model.codebase.Commit;
import com.insightservice.springboot.model.codebase.FileObject;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CommitRepository extends MongoRepository<Commit, String>
{
}