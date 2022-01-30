package com.insightservice.springboot.service;

import com.insightservice.springboot.model.User;
import com.insightservice.springboot.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DatabaseDemoService
{
    @Autowired
    UserRepository userRepository;

    public User createUser(User user)
    {
        return userRepository.insert(user);
    }

    public Iterable<User> getAllUsers()
    {
        return userRepository.findAll();
    }
}
