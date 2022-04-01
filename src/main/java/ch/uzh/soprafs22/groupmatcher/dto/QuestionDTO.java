package ch.uzh.soprafs22.groupmatcher.dto;

import ch.uzh.soprafs22.groupmatcher.constant.QuestionCategory;
import ch.uzh.soprafs22.groupmatcher.constant.QuestionType;
import ch.uzh.soprafs22.groupmatcher.model.Answer;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class QuestionDTO {
    private Integer ordinalNum;
    private String content;
    private Float weight;
    private QuestionCategory questionCategory;
    private QuestionType questionType;
    private List<Answer> answers;
}
