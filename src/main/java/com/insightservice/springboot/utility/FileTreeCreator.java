package com.insightservice.springboot.utility;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.insightservice.springboot.model.codebase.Codebase;
import com.insightservice.springboot.model.codebase.FileObject;
import com.insightservice.springboot.model.file_tree.RepoPackage;
import com.insightservice.springboot.model.file_tree.RepoTreeNode;

import java.io.File;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.regex.Pattern;

import static com.insightservice.springboot.Constants.LOG;

public class FileTreeCreator
{
    private FileTreeCreator() {
        //This is a utility class
    }

    /**
     * Creates a tree structure of files.
     * Each package/directory becomes a RepoPackage, while FileObjects are leaf nodes.
     * @param fileObjectSet a collection of all files to represent in the tree.
     * @return a RepoPackage representing the root directory, i.e. "."
     */
    public static RepoPackage createFileTree(HashSet<FileObject> fileObjectSet)
    {
        LOG.info("Creating file tree...");
        RepoPackage root = new RepoPackage(Path.of("."));

        for (FileObject fileObject : fileObjectSet)
        {
            String[] splitPath = fileObject.getPath().toString().split(Pattern.quote(File.separator));
            insertFile(root, fileObject.getPath().toString(), splitPath, 0, fileObject);
        }
        LOG.info("Done. File tree created.");

        return root;
    }

    /**
     * A brute-force approach to placing a FileObject into a RepoPackage file tree.
     * @param currentDirectory the root RepoPackage or, during recursion, a sub-RepoPackage (subdirectory)
     * @param fullPath a path relative to the root of the file tree. Ex: "src/main/java/Main.java"
     * @param splitPath the fullPath, but split by a path separator like / or \
     * @param splitIndex zero. Incremented once every recursion call.
     * @param fileToInsert
     */
    private static void insertFile(RepoPackage currentDirectory, String fullPath, String[] splitPath, int splitIndex, FileObject fileToInsert)
    {
        //If the string in the splitPath is not the last one, we're in a directory
        if (splitIndex < splitPath.length - 1)
        {
            //Check if already exists
            RepoTreeNode existingDir = currentDirectory.getFileTreeNodeList().stream()
                    .filter(repoTreeNode -> repoTreeNode instanceof RepoPackage && fullPath.contains(repoTreeNode.getPath().toString()))
                    .findAny()
                    .orElse(null);
            if (existingDir == null)
            {
                //mkdir
                String dirName = splitPath[splitIndex];
                RepoPackage subDirectory = new RepoPackage(Path.of(dirName));
                currentDirectory.addFileTreeNode(subDirectory);

                splitIndex++; //descend into the new dir
                insertFile(subDirectory, fullPath, splitPath, splitIndex, fileToInsert);
            }
            else
            {
                splitIndex++; //descend into the existing dir
                insertFile((RepoPackage) existingDir, fullPath, splitPath, splitIndex, fileToInsert);
            }
        }
        //If we're at the end of the splitPath, we have a file
        else
        {
            //Add the file to the dir
            currentDirectory.addFileTreeNode(fileToInsert);
        }
    }

    public static void main(String[] args) throws JsonProcessingException {
        Codebase codebase = new Codebase();
        codebase.getActiveFileObjects().add(new FileObject(Path.of("src/MyClass.java")));
        codebase.getActiveFileObjects().add(new FileObject(Path.of("src/model/MyModel.java")));
        RepoPackage output = createFileTree(codebase.getActiveFileObjects());

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        System.out.println(objectMapper.writeValueAsString(output));
    }
}
