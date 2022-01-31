package com.insightservice.springboot.model.codebase;

import com.insightservice.springboot.Constants;

import javax.validation.constraints.NotNull;
import java.util.Objects;
import java.util.TreeSet;

public class CommitGroupingKey implements Comparable<CommitGroupingKey>
{
    private final int maxCommonCommits;
    private final TreeSet<FileObject> fileObjects;

    public CommitGroupingKey(TreeSet<FileObject> fileObjects, int maxCommonCommits)
    {
        this.maxCommonCommits = maxCommonCommits;
        this.fileObjects = fileObjects;
    }

    @Override
    public String toString() {
        return maxCommonCommits + Constants.SEPARATOR + fileObjects.hashCode();
    }

    @Override
    public int compareTo(@NotNull CommitGroupingKey other) {
        return Integer.compare(this.maxCommonCommits, other.maxCommonCommits);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommitGroupingKey that = (CommitGroupingKey) o;
        return maxCommonCommits == that.maxCommonCommits && Objects.equals(fileObjects, that.fileObjects);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maxCommonCommits, fileObjects);
    }
}
