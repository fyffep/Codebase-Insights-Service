package com.insightservice.springboot.model.file_tree;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class RepoPackage implements RepoTreeNode
{
    private Path path;
    private List<RepoTreeNode> fileTreeNodeList;

    public RepoPackage(Path path) {
        this.path = path;
        fileTreeNodeList = new ArrayList<>();
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public List<RepoTreeNode> getFileTreeNodeList() {
        return fileTreeNodeList;
    }

    public void setFileTreeNodeList(List<RepoTreeNode> fileTreeNodeList) {
        this.fileTreeNodeList = fileTreeNodeList;
    }

    public void addFileTreeNode(RepoTreeNode fileTreeNode) {
        this.fileTreeNodeList.add(fileTreeNode);
    }
}
