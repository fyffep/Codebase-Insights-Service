package com.insightservice.springboot.repository;

import com.insightservice.springboot.model.codebase.Codebase;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CodebaseRepository extends MongoRepository<Codebase, String>
{
}