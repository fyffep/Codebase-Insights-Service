package com.insightservice.springboot.controller;


import com.google.gson.Gson;
import com.insightservice.springboot.model.sonar.Issue;
import com.insightservice.springboot.payload.SettingsPayload;

import com.insightservice.springboot.service.SonarQubeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import static com.insightservice.springboot.Constants.LOG;

import java.io.IOException;
import java.util.HashMap;

@RestController
@RequestMapping("/api/sonar")
public class SonarQubeController {


    @GetMapping("/issues")
    public ResponseEntity<?> getAllIssues(@RequestBody SettingsPayload urlPayload, BindingResult result) throws IOException {

        String project = urlPayload.getGithubUrl();
        HashMap<String, Integer> componentIssueCount = new HashMap<>();
        HashMap<String, Integer> authorIssueCount = new HashMap<>();

        // extract the project name from the GitHub URL
        project = "patient-manager";

        LOG.info("Getting all sonar issues for " + project);

        // call the SonarQube Service and format the response
        try {
            Issue[] issues = SonarQubeService.getInstance().getIssues(project);

            // aggregate based on the component
            for (Issue issue : issues) {
                if (!componentIssueCount.containsKey(issue.component)) {
                    componentIssueCount.put(issue.component, 1);
                }
                componentIssueCount.put(issue.component, componentIssueCount.get(issue.component) + 1);

                if (!authorIssueCount.containsKey(issue.component)) {
                    authorIssueCount.put(issue.component, 1);
                }
                authorIssueCount.put(issue.component, authorIssueCount.get(issue.component) + 1);
            }
            GetIssuesResponse getIssuesResponse = new GetIssuesResponse();

            int authorSize = authorIssueCount.size();
            int componentSize = componentIssueCount.size();

            getIssuesResponse.authors = new Author[authorSize];
            getIssuesResponse.issues = new Component[componentSize];

            int i = 0;
            for(String author : authorIssueCount.keySet()) {
                getIssuesResponse.authors[i] = new Author(author, authorIssueCount.get(author));
                i++;
            }


            i = 0;
            for(String issue : componentIssueCount.keySet()) {
                getIssuesResponse.issues[i] = new Component(issue, componentIssueCount.get(issue));
                i++;
            }


            Gson gson = new Gson();

            String response = gson.toJson(getIssuesResponse);

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }


    }



}

class GetIssuesResponse {
    Component[] issues;
    Author[] authors;

}

class Component {
    String component;

    public Component(String component, int count) {
        this.component = component;
        this.count = count;
    }

    int count;
}

class Author {
    String author;
    int count;

    public Author(String author, int count) {
        this.author = author;
        this.count = count;
    }
}