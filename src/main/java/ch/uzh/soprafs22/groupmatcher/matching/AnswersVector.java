package ch.uzh.soprafs22.groupmatcher.matching;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.math3.ml.clustering.Clusterable;

@Data
@AllArgsConstructor
public class AnswersVector implements Clusterable {

    private String studentName;

    private double[] vector;

    @Override
    public double[] getPoint() {
        return vector;
    }
}
