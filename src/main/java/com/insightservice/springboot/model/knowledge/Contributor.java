package com.insightservice.springboot.model.knowledge;

import java.util.HashSet;
import java.util.Set;

public class Contributor
{
    private int id;
    private String email;
    private int knowledgeScore;
    private Set<String> filesKnown; //these should be names of files

    /**
     * Bean constructor
     */
    public Contributor() {
    }

    public Contributor(int id, String email, int knowledgeScore) {
        this.id = id;
        this.email = email;
        this.knowledgeScore = knowledgeScore;
        filesKnown = new HashSet<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getKnowledgeScore() {
        return knowledgeScore;
    }

    public void setKnowledgeScore(int knowledgeScore) {
        this.knowledgeScore = knowledgeScore;
    }

    public Set<String> getFilesKnown() {
        return filesKnown;
    }

    public void setFilesKnown(Set<String> filesKnown) {
        this.filesKnown = filesKnown;
    }
}