package com.insightservice.springboot.model.sonar;

import java.util.Arrays;

public class IssueResponse {
    public int total;
    public int p;
    public int ps;
    public Paging paging;
    public int effortTotal;
    public Issue[] issues;

    @Override
    public String toString() {
        return "IssueResponse{" +
                "total=" + total +
                ", p=" + p +
                ", ps=" + ps +
                ", paging=" + paging +
                ", effortTotal=" + effortTotal +
                ", issues=" + Arrays.toString(issues) +
                '}';
    }
}



class Paging {
    int pageIndex;
    int pageSize;
    int total;

}
