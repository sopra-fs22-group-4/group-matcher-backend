package ch.uzh.soprafs22.groupmatcher.dto;

import ch.uzh.soprafs22.groupmatcher.constant.QuestionCategory;
import ch.uzh.soprafs22.groupmatcher.constant.QuestionType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class QuestionDTO {
    private Integer ordinalNum;
    private String content;
    private QuestionType questionType;
    private QuestionCategory questionCategory;
    private List<String> answers;
}
