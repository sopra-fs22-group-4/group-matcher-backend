package ch.uzh.soprafs22.groupmatcher.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
public class Student {
    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String email;

    private String name;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "matcher_id")
    private Matcher matcher;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;

    @JsonFormat(pattern = "dd.MM.yyyy")
    private ZonedDateTime submissionTimestamp;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "student_answers",
            joinColumns = @JoinColumn(name = "student_id"),
            inverseJoinColumns = @JoinColumn(name = "answers_id"))
    private List<Answer> selectedAnswers = new ArrayList<>();

    @JsonProperty("questions")
    public List<Question> getQuestions() {
        return matcher.getQuestions();
    }

}
