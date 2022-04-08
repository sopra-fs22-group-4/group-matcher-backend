package ch.uzh.soprafs22.groupmatcher.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
public class QuestionRes {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "matcher_id")
    private Matcher matcher;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private Student student;

    @Column(nullable = false)
    private Timestamp createdAt;

    @ManyToMany
    @JoinTable(name = "QuestionRes_Answers",
            joinColumns = @JoinColumn(name="QuestionsRes_id"),
            inverseJoinColumns = @JoinColumn(name="Answer_id"))
    private List<Answer> answers = new ArrayList<>();

    @Column(nullable = false)
    private boolean verified = false;
}
