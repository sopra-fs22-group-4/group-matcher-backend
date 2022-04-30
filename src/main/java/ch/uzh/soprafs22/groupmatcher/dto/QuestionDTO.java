package ch.uzh.soprafs22.groupmatcher.dto;

import ch.uzh.soprafs22.groupmatcher.constant.QuestionType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class QuestionDTO {
    private Integer ordinalNum;
    private String content;
    private Double weight = 1.0;
    private QuestionType questionType;
    private List<String> answers = new ArrayList<>();
}
