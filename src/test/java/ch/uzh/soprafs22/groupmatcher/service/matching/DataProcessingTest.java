package ch.uzh.soprafs22.groupmatcher.service.matching;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DataProcessingTest {

    DataProcessing dataProcessing = new DataProcessing();
    double[][] answersMatrix;
    String[] studentEmails;

    @BeforeEach
    public void init(){
        answersMatrix = new double[][]{
                {1, 0, 0},
                {0, 1, 0},
                {0, 0, 1}
        };

        studentEmails = new String[]{"test1","test2","test3"};
    }

    @Test
    void adjMatrixToVertexList_sim() {
        double[][] totalScoreMatrix = dataProcessing.calMatchingScore(answersMatrix,true);
        List<Vertex> vertices = dataProcessing.adjMatrixToVertexList(totalScoreMatrix, studentEmails);
        Prim prim = new Prim(vertices);
        System.out.println("sim");
        System.out.println(prim.originalGraphToString());

        assertEquals(3,vertices.size());

        assertEquals(2,vertices.get(0).getEdges().size());
        assertEquals(2,vertices.get(1).getEdges().size());
        assertEquals(2,vertices.get(2).getEdges().size());

        assertNull(vertices.get(0).getEdges().get(0));
        assertEquals(1,vertices.get(0).getEdges().get(vertices.get(1)).getWeight());
        assertEquals(1,vertices.get(0).getEdges().get(vertices.get(2)).getWeight());
    }

    @Test
    void adjMatrixToVertexList_diff() {
        double[][] totalScoreMatrix = dataProcessing.calMatchingScore(answersMatrix,false);
        List<Vertex> vertices = dataProcessing.adjMatrixToVertexList(totalScoreMatrix, studentEmails);
        Prim prim = new Prim(vertices);
        System.out.println("diff");
        System.out.println(prim.originalGraphToString());

        assertEquals(3,vertices.size());

        assertEquals(2,vertices.get(0).getEdges().size());
        assertEquals(2,vertices.get(1).getEdges().size());
        assertEquals(2,vertices.get(2).getEdges().size());

        assertNull(vertices.get(0).getEdges().get(0));
        assertEquals(2,vertices.get(0).getEdges().get(vertices.get(1)).getWeight());
        assertEquals(2,vertices.get(0).getEdges().get(vertices.get(2)).getWeight());
    }

    @Test
    void adjMatrixToVertexList_total() {
        double[][] simScoreMatrix = dataProcessing.calMatchingScore(answersMatrix,true);
        double[][] diffScoreMatrix = dataProcessing.calMatchingScore(answersMatrix,false);
        double[][] totalScoreMatrix = dataProcessing.matrixAddition(simScoreMatrix,diffScoreMatrix);
        List<Vertex> vertices = dataProcessing.adjMatrixToVertexList(totalScoreMatrix, studentEmails);
        Prim prim = new Prim(vertices);
        System.out.println("total");
        System.out.println(prim.originalGraphToString());

        assertEquals(3,vertices.size());

        assertEquals(2,vertices.get(0).getEdges().size());
        assertEquals(2,vertices.get(1).getEdges().size());
        assertEquals(2,vertices.get(2).getEdges().size());

        assertNull(vertices.get(0).getEdges().get(0));
        assertEquals(3,vertices.get(0).getEdges().get(vertices.get(1)).getWeight());
        assertEquals(3,vertices.get(0).getEdges().get(vertices.get(2)).getWeight());
    }

    @Test
    void matrixCornerCases(){

        double[][] simScoreMatrix = dataProcessing.calMatchingScore(answersMatrix,true);
        double[][] diffScoreMatrix = dataProcessing.calMatchingScore(answersMatrix,false);
        double[][] emptyArray = new double[0][];

        double[][] emptySimScoreMatrix = dataProcessing.calMatchingScore(emptyArray,true);
        double[][] test1 = dataProcessing.matrixAddition(emptySimScoreMatrix, diffScoreMatrix);
        assertEquals(diffScoreMatrix[0][1], test1[0][1]);

        double[][] emptyDiffScoreMatrix = dataProcessing.calMatchingScore(emptyArray,true);
        double[][] test2 = dataProcessing.matrixAddition(simScoreMatrix, emptyDiffScoreMatrix);
        assertEquals(simScoreMatrix[0][1], test2[0][1]);

        double[][] test3 = dataProcessing.matrixAddition(simScoreMatrix, emptyArray);
        assertEquals(simScoreMatrix[0][1], test3[0][1]);

        double[][] test4 = dataProcessing.matrixAddition(emptyArray, diffScoreMatrix);
        assertEquals(diffScoreMatrix[0][1], test4[0][1]);

    }


}