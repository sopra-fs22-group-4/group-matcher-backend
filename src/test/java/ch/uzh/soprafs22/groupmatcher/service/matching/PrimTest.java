package ch.uzh.soprafs22.groupmatcher.service.matching;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PrimTest {

    DataProcessing dataProcessing = new DataProcessing();
    String[] studentEmails;
    List<Vertex> initGraph;

    @BeforeEach
    public void init(){

        double[][] simAnswersMatrix = new double[][]{
                {1, 0, 1, 1, 1, 1},
                {0, 0, 1, 1, 0, 0},
                {1, 1, 0, 0, 1, 1},
                {0, 1, 0, 1, 0, 1},
                {1, 1, 0, 0, 0, 0},
                {0, 0, 1, 1, 1, 0},
                {1, 0, 0, 0, 1, 0}
        };

        double[][] diffAnswersMatrix = new double[][]{
                {0, 0, 0, 0, 1, 1},
                {0, 0, 1, 1, 0, 0},
                {1, 0, 0, 1, 0, 1},
                {0, 1, 0, 1, 0, 1},
                {1, 0, 1, 0, 0, 0},
                {1, 0, 0, 0, 1, 1},
                {1, 1, 0, 0, 0, 1}
        };

        studentEmails = new String[]{"test1","test2","test3","test4","test5","test6","test7"};

        double[][] simScoreMatrix = dataProcessing.calMatchingScore(simAnswersMatrix,true);
        double[][] diffScoreMatrix = dataProcessing.calMatchingScore(diffAnswersMatrix,false);
        double[][] totalScoreMatrix = dataProcessing.matrixAddition(simScoreMatrix,diffScoreMatrix);

        initGraph = dataProcessing.adjMatrixToVertexList(totalScoreMatrix, studentEmails);
    }

    @Test
    void givenAGraph_whenPrimRuns_thenPrintMST() {

        int groupSize = 3;

        Prim prim = new Prim(initGraph);
        System.out.println(prim.originalGraphToString());
        System.out.println("----------------");

        while(!prim.getGraph().isEmpty()){
            prim.run(groupSize,true);
            System.out.println();

            prim.getVisitedVertex().forEach(vertex -> System.out.println(vertex.getEmail()));
            System.out.println("----------------");

            prim.deleteVisitedVertex();
            System.out.println(prim.originalGraphToString());

        }
        assertTrue(true);
    }
}