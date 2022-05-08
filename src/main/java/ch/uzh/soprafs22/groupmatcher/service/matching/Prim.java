package ch.uzh.soprafs22.groupmatcher.service.matching;

import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Component;

/*
* https://www.baeldung.com/java-prim-algorithm#:~:text=2.,edges%20is%20at%20a%20minimum.
* Minimum spanning tree -> Maximum spanning tree
* */

@Getter
@Setter
@Component
public class Prim {
    private List<Vertex> graph;

    public Prim(List<Vertex> graph){
        this.graph = graph;
    }

    public void run(int groupSize, boolean verbose){

        if (!graph.isEmpty()){
            graph.get(0).setVisited(true);
        }

        while (isDisconnected() && countVisited()<groupSize){
            Edge nextMaximum = new Edge(Integer.MIN_VALUE);
            Vertex nextVertex = graph.get(0);
            for (Vertex vertex : graph){
                if (vertex.isVisited()){
                    Pair<Vertex, Edge> candidate = vertex.nextMaximum();
                    if (candidate.getValue().getWeight() > nextMaximum.getWeight()){
                        nextMaximum = candidate.getValue();
                        nextVertex = candidate.getKey();
                    }
                }
            }
            nextMaximum.setIncluded(true);
            nextVertex.setVisited(true);
        }
        if(verbose){
            resetPrintHistory();
        }
    }

    private boolean isDisconnected(){
        for (Vertex vertex : graph){
            if (!vertex.isVisited()){
                return true;
            }
        }
        return false;
    }

    private int countVisited(){
        int count = 0;
        for (Vertex vertex : graph){
            if (vertex.isVisited()){
                count++;
            }
        }
        return count;
    }

    public void deleteVisitedVertex() {
        graph.removeIf(Vertex::isVisited);
    }

    public List<Vertex> getVisitedVertex(){
        return graph.stream().filter(Vertex::isVisited).toList();
    }

    public String originalGraphToString(){
        StringBuilder sb = new StringBuilder();
        for (Vertex vertex : graph){
            sb.append(vertex.originalToString());
        }
        return sb.toString();
    }

    public void resetPrintHistory(){
        for (Vertex vertex : graph){
            for (Map.Entry<Vertex, Edge> pair : vertex.getEdges().entrySet()) {
                pair.getValue().setPrinted(false);
            }
        }
    }

    public String minimumSpanningTreeToString(){
        StringBuilder sb = new StringBuilder();
        for (Vertex vertex : graph){
            sb.append(vertex.includedToString());
        }
        return sb.toString();
    }
}
