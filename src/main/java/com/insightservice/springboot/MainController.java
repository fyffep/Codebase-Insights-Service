package com.insightservice.springboot;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Abhishek Tiwari
 * 01/27/22
 */

@RestController
public class MainController {

    @GetMapping("/")
    public String healthCheck() {
        return "Your application is live and running";
    }
}
