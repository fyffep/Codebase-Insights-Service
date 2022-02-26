package com.insightservice.springboot.repository;

import com.insightservice.springboot.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface CodebaseRepository extends MongoRepository<User, String>
{
}