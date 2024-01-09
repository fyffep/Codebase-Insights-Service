package com.insightservice.springboot.utility;

import java.io.File;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.regex.Pattern;

import com.insightservice.springboot.model.codebase.FileObject;
import com.insightservice.springboot.model.file_tree.RepoFile;
import com.insightservice.springboot.model.file_tree.RepoPackage;
import com.insightservice.springboot.model.file_tree.RepoTreeNode;

public class FileTreeCreatorRefactor {
    public static RepoTreeNode createTreeFromList(HashSet<FileObject> fileObjects) {
        RepoPackage root = new RepoPackage(Path.of("."));

        RepoPackage current = null;

        for (FileObject fileObject : fileObjects) {
            String[] splitPath = fileObject.getPath().toString().split(Pattern.quote("\\"));
            if (splitPath.length > 1) {
                current = FileTreeCreatorRefactor._add_directory(root, (splitPath));
            }
            ;

            String filenameFromPath = splitPath[splitPath.length - 1];

            String filename = fileObject.getFilename().toString();

            assert filenameFromPath.equals(filename);

            if (current != null) {
                current.addFileTreeNode(new RepoFile(fileObject.getPath(), filename));
            } else {
                root.addFileTreeNode(new RepoFile(fileObject.getPath(), filename));
            }

        }

        return root;

    }

    private static RepoPackage _add_directory(RepoPackage root, String[] directories) {
        // Search like you'd search on a TrieNode

        RepoPackage current = root;

        for (int i = 0; i < directories.length - 2; i++) {
            String directory = directories[i];
            RepoPackage childRepo = (RepoPackage) current.getFileTreeNodeList().stream()
                    .filter(repoTreeNode -> repoTreeNode instanceof RepoPackage
                            && repoTreeNode.getPath().toString().contains(directory))
                    .findAny()
                    .orElse(null);

            if (childRepo == null) {
                childRepo = new RepoPackage(Path.of(directory));
                current.addFileTreeNode(childRepo);
            } else {
                current = childRepo;
            }
        }

        return current;

    }
}
