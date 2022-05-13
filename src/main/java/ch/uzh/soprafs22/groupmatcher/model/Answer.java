package ch.uzh.soprafs22.groupmatcher.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
public class Answer {
    @Id
    @GeneratedValue
    private Long id;

    private String content;

    private Integer ordinalNum;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "question_id")
    private Question question;

    @JsonIgnore
    @ManyToMany(mappedBy = "selectedAnswers", cascade = CascadeType.ALL)
    private List<Student> students = new ArrayList<>();

    public Double calculateBalancedMatchingScore(Long otherStudentId) {
        boolean isSharedAnswer = students.stream().anyMatch(student -> student.getId().equals(otherStudentId));
        return question.getQuestionCategory().calculateBalancedMatchingScore(isSharedAnswer);
    }
}