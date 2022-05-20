package ch.uzh.soprafs22.groupmatcher.model;

import ch.uzh.soprafs22.groupmatcher.constant.QuestionCategory;
import ch.uzh.soprafs22.groupmatcher.constant.QuestionType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
public class Question {
    @Id
    @GeneratedValue
    private Long id;

    private String content;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private QuestionType questionType = QuestionType.SINGLE_CHOICE;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private QuestionCategory questionCategory;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "matcher_id")
    private Matcher matcher;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL)
    private List<Answer> answers = new ArrayList<>();
}