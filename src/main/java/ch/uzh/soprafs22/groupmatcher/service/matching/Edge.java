package ch.uzh.soprafs22.groupmatcher.service.matching;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Edge {

    private int weight;
    private boolean isIncluded = false;
    private boolean isPrinted = false;

    public Edge(int weight) {
        this.weight = weight;
    }
}
