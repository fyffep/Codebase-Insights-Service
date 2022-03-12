package com.insightservice.springboot.utility;

import java.util.List;

public class GitIgnoreFilter
{
    //for files: check if the file name ends with any one of these
    private static final List<String> DEFAULT_IGNORE_EXTENSIONS = List.of(
            ".gitignore",
            /* COMPILED FILES */
            ".chunk.css",
            ".chunk.css.map",
            ".jar",
            /* IMAGES */
            ".png",
            ".svg",
            ".ico",
            ".jpg",
            ".jpeg",
            /* MISC */
            ".lnk" //Windows shortcut file
    );

    //for directories: check if the file path contains any one of these
    private static final List<String> DEFAULT_IGNORE_IF_CONTAINS = List.of(
            /* IDE FILES */
            ".idea/",
            ".eclipse/",
            ".vscode/",
            ".vs/",
            ".settings/",
            /* DEPENDENCY FILES */
            "node_modules/",
            /* COMPILED FILES */
            "target/",
            ".chunk.js",
            ".js~"
            /* MISC */
    );

    public static boolean isIgnored(String filePath)
    {
        for (String extensionToIgnore : DEFAULT_IGNORE_EXTENSIONS)
        {
            if (filePath.endsWith(extensionToIgnore))
                return true;
        }
        for (String extensionToIgnore : DEFAULT_IGNORE_IF_CONTAINS)
        {
            if (filePath.contains(extensionToIgnore))
                return true;
        }
        return false;
    }
}
