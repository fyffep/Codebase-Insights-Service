package com.insightservice.springboot.model.knowledge;

public class Contributor
{
    private int id;
    private String email;
    private int knowledgeScore;

    public Contributor(int id, String email, int knowledgeScore) {
        this.id = id;
        this.email = email;
        this.knowledgeScore = knowledgeScore;
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
}