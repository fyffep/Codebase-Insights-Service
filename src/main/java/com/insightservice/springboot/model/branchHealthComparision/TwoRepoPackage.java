package com.insightservice.springboot.model.branchHealthComparision;

import com.insightservice.springboot.model.file_tree.RepoPackage;

public class TwoRepoPackage {
    RepoPackage package1;
    RepoPackage package2;

    public TwoRepoPackage(RepoPackage package1, RepoPackage package2) {
        this.package1 = package1;
        this.package2 = package2;
    }
}
