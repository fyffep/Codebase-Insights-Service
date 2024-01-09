package com.insightservice.springboot.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.insightservice.springboot.model.fileSimilarity.Directory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insightservice.springboot.model.fileSimilarity.fileLists;
import static com.insightservice.springboot.Constants.LOG;

@Service
public class FileSimilarityService {
    private final RestTemplate restTemplate;

    public FileSimilarityService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = new RestTemplate();
    }

    public fileLists findSimilarity(String repoPath) {

        repoPath = System.getProperty("user.dir") + "/" + repoPath;

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.add("user-agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36");

        Map<String, String> map = new HashMap<>();
        map.put("directory", repoPath);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(map, headers);

        ResponseEntity<fileLists> result = this.restTemplate.postForEntity(
                URI.create("http://localhost:3000/findSimilarity/directory"),
                entity, fileLists.class);

        System.out.println(result.getBody());

        return result.getBody();
    }
}
