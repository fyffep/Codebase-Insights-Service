package com.insightservice.springboot.repository;

import com.insightservice.springboot.model.User;
import org.springframework.data.mongodb.repository.Query;


import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, String>
{
    @Query("{ 'username' : ?0 }")
    User findByUsername(String username);
}