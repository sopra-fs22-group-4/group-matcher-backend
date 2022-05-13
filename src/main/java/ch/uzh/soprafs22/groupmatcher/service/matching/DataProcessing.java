package ch.uzh.soprafs22.groupmatcher.service.matching;

import ch.uzh.soprafs22.groupmatcher.model.Student;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.linear.MatrixUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class DataProcessing {

    public List<Vertex> adjMatrixToVertexList(double[][] scoreMatrix, String[] studentEmails){
        // pre-condition : scoreMatrix should be square matrix
        List<Vertex> graph = new ArrayList<>();
        List<Vertex> vertices = Arrays.stream(studentEmails).map(Vertex::new).toList();

        for (int i=0; i<vertices.size(); i++){
            Vertex a = vertices.get(i);
            for (int j=i+1; j<vertices.size(); j++){
                Vertex b = vertices.get(j);
                Edge ab = new Edge((int) scoreMatrix[i][j]);
                a.addEdge(b, ab);
                b.addEdge(a, ab);
            }
            graph.add(a);
        }

        return graph;
    }

    public double[][] calMatchingScoreTemp(List<Student> students){
        int totalStudents = students.size();
        double[][] scoreMatrix = new double[totalStudents][totalStudents];
        for (int i=0; i<totalStudents; i++){
            for (int j=1; j<totalStudents; j++){
                if(i!=j){
                    Student student1 = students.get(i);
                    Student student2 = students.get(j);
                    double matchingScore = student1.getSelectedAnswers().stream().mapToDouble(selectedAnswer ->
                            selectedAnswer.calculateBalancedMatchingScore(student2.getId())).sum();
                    scoreMatrix[i][j] = matchingScore;
                }
            }
        }
        double[][] scoreMatrixTranspose = MatrixUtils.createRealMatrix(scoreMatrix).transpose().getData();

        return matrixAddition(scoreMatrix,scoreMatrixTranspose);
    }

    public double[][] calMatchingScore(double[][] answersMatrix, boolean similarityBase){
        if(ArrayUtils.isNotEmpty(answersMatrix)){
            double[][] scoreMatrix = new double[answersMatrix.length][answersMatrix.length];
            for (int i=0; i<answersMatrix.length; i++){
                for (int j=i+1; j<answersMatrix.length; j++){
                    if (similarityBase){
                        scoreMatrix[i][j] = (int) Arrays.stream(xnor(answersMatrix[i],answersMatrix[j]))
                                .filter(value -> value).count();
                    }else{
                        scoreMatrix[i][j] = (int) Arrays.stream(xor(answersMatrix[i],answersMatrix[j]))
                                .filter(Boolean::booleanValue).count();
                    }
                }
            }
            return scoreMatrix;
        }else{
            return new double[0][];
        }
    }

    public double[][] matrixAddition(double[][] a, double[][] b){
        if (ArrayUtils.isNotEmpty(a) && ArrayUtils.isNotEmpty(b)){
              double[][] result = new double[a.length][a.length];
            for (int i=0; i<a.length; i++){
                for (int j=0; j<a.length; j++){
                    result[i][j] = a[i][j] + b[i][j];
                }
            }
            return result;
        }else{
            return ArrayUtils.isNotEmpty(a)? a : b;
        }
    }

    Boolean[] xnor(double[] a, double[] b) {
        Boolean[] result = new Boolean[a.length];
        for (int i=0; i<a.length; i++){
            result[i] = a[i] == b[i];
        }
        return result;
    }

    Boolean[] xor(double[] a, double[] b) {
        Boolean[] result = new Boolean[a.length];
        for (int i=0; i<a.length; i++){
            result[i] = a[i] != b[i];
        }
        return result;
    }
}
