package com.insightservice.springboot.model.knowledge;

import java.util.List;
import java.util.Set;

public class AdvancedContributor {
    private List<Integer> sets;
    private Set<String> fileSets;
    private String label;
    private int size;

    public List<Integer> getSets() {
        return sets;
    }

    public void setSets(List<Integer> sets) {
        this.sets = sets;
    }

    public Set<String> getFileSets() {
        return fileSets;
    }

    public void setFileSets(Set<String> fileSets) {
        this.fileSets = fileSets;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
