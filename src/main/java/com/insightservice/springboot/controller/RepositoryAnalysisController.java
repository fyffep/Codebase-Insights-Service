package com.insightservice.springboot.controller;

import com.insightservice.springboot.payload.UrlResquest;
import com.insightservice.springboot.service.RepositoryAnalysisService;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;

@RestController
@RequestMapping("/api/analysis")
public class RepositoryAnalysisController
{
    @Autowired
    private RepositoryAnalysisService repositoryAnalysisService;

    //TEMPORARY! Should be changed to do more than just clone. TODO
    @GetMapping("/clone-repository/")
    public ResponseEntity<?> cloneMyRepository(@Valid @RequestBody UrlResquest urlResquest, BindingResult bindingResult) throws GitAPIException, IOException
    {
        repositoryAnalysisService.cloneMyRepository(urlResquest.getUrl());

        return new ResponseEntity<String>("Successfully cloned your repository.", HttpStatus.OK);
    }

    //Unused
    /*@PostMapping("/clone-repository/${remoteUrl}")
    public ResponseEntity<?> cloneMyRepository(@PathVariable remoteUrl) throws GitAPIException, IOException
    {
        repositoryAnalysisService.cloneMyRepository(remoteUrl);

        return new ResponseEntity<String>("Successfully cloned your repository.", HttpStatus.OK);
    }*/
}
