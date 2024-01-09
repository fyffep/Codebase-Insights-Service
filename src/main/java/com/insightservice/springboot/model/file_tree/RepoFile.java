package com.insightservice.springboot.model.file_tree;

import java.nio.file.Path;

public class RepoFile implements RepoTreeNode {
    Path path;
    String filename;

    public RepoFile(Path path, String filename) {
        this.path = path;
        this.filename = filename;
    }

    public Path getPath() {
        return this.path;
    }
}
