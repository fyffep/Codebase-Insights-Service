package com.insightservice.springboot.repository;

import com.insightservice.springboot.model.codebase.Codebase;
import com.insightservice.springboot.model.codebase.FileObject;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface FileObjectRepository extends MongoRepository<FileObject, String>
{
}