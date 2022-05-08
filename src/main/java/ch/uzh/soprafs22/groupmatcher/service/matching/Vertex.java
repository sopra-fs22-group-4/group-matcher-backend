package ch.uzh.soprafs22.groupmatcher.service.matching;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;
import org.apache.commons.math3.util.Pair;

@Getter
@Setter
public class Vertex {
    private String email;
    private Map<Vertex, Edge> edges = new HashMap<>();
    private boolean isVisited = false;
    private String divideLine = " --- ";

    public Vertex(String email){
        this.email = email;
    }

    public void addEdge(Vertex vertex, Edge edge){
        if (this.edges.containsKey(vertex)){
            if (edge.getWeight() < this.edges.get(vertex).getWeight()){
                this.edges.replace(vertex, edge);
            }
        } else {
            this.edges.put(vertex, edge);
        }
    }

    // CHECKPOINT
    public Pair<Vertex, Edge> nextMaximum(){
        Edge nextMaximum = new Edge(Integer.MIN_VALUE);
        Vertex nextVertex = this;
        for (Map.Entry<Vertex, Edge> pair : edges.entrySet()) {
            if (!pair.getKey().isVisited()
                    && !pair.getValue().isIncluded()
                    && pair.getValue().getWeight() > nextMaximum.getWeight()) {
                        nextMaximum = pair.getValue();
                        nextVertex = pair.getKey();
            }
        }

        return new Pair<>(nextVertex, nextMaximum);
    }

    public String originalToString(){
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<Vertex, Edge> pair : edges.entrySet()) {
            if (!pair.getValue().isPrinted()) {
                sb.append(getEmail());
                sb.append(divideLine);
                sb.append(pair.getValue().getWeight());
                sb.append(divideLine);
                sb.append(pair.getKey().getEmail());
                sb.append("\n");
                pair.getValue().setPrinted(true);
            }
        }
        return sb.toString();
    }

    public String includedToString(){
        StringBuilder sb = new StringBuilder();
        if (isVisited()) {
            for (Map.Entry<Vertex, Edge> pair : edges.entrySet()) {
                if (pair.getValue().isIncluded()
                    && !pair.getValue().isPrinted()) {
                        sb.append(getEmail());
                        sb.append(divideLine);
                        sb.append(pair.getValue().getWeight());
                        sb.append(divideLine);
                        sb.append(pair.getKey().getEmail());
                        sb.append("\n");
                        pair.getValue().setPrinted(true);
}
            }
        }

        return sb.toString();
    }
}
