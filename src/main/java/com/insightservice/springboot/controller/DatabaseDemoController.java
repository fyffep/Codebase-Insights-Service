package com.insightservice.springboot.controller;

import com.insightservice.springboot.model.User;
import com.insightservice.springboot.service.DatabaseDemoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * A temporary controller to show that we can use MongoDB.
 */
@RestController
@RequestMapping("/api/data")
public class DatabaseDemoController
{
    @Autowired
    private DatabaseDemoService databaseDemoService;

    @PostMapping("/create")
    public ResponseEntity<?> createUser(@Valid @RequestBody User user, BindingResult result)
    {
        User newUser = databaseDemoService.createUser(user);

        return new ResponseEntity<User>(newUser, HttpStatus.OK);
    }

    @GetMapping("/all")
    public Iterable<User> getAllUsers()
    {
        return databaseDemoService.getAllUsers();
    }
}
