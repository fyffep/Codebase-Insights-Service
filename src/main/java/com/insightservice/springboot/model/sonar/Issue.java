package com.insightservice.springboot.model.sonar;

import java.util.Arrays;

public class Issue {
    public String key;
    public String rule;
    public String severity;
    public String component;
    public String project;
    public int line;
    public String hash;
    public TextRange textRange;
    //    String [] flows;
    public String status;
    public String message;
    public String effort;
    public String debt;
    public String author;
    public String[] tags;
    public String creationDate;
    public String updateDate;
    public String type;
    public String scope;

    @Override
    public String toString() {
        return "Issue{" +
                "key='" + key + '\'' +
                ", rule='" + rule + '\'' +
                ", severity='" + severity + '\'' +
                ", component='" + component + '\'' +
                ", project='" + project + '\'' +
                ", line=" + line +
                ", hash='" + hash + '\'' +
                ", textRange=" + textRange +
//                ", flows=" + Arrays.toString(flows) +
                ", status='" + status + '\'' +
                ", message='" + message + '\'' +
                ", effort='" + effort + '\'' +
                ", debt='" + debt + '\'' +
                ", author='" + author + '\'' +
                ", tags=" + Arrays.toString(tags) +
                ", creationDate='" + creationDate + '\'' +
                ", updateDate='" + updateDate + '\'' +
                ", type='" + type + '\'' +
                ", scope='" + scope + '\'' +
                '}';
    }
}

class TextRange {
    int startLine;
    int endLine;
    int startOffset;
    int endOffset;
}