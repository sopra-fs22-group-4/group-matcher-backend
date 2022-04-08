package ch.uzh.soprafs22.groupmatcher.model;

import ch.uzh.soprafs22.groupmatcher.constant.QuestionCategory;
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

    @Column(nullable = false)
    private Integer ordinalNum;

    @Column(nullable = false)
    private String content;

    private Float weight;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private QuestionCategory questionCategory;

    @ManyToOne
    @JoinColumn(name = "matcher_id")
    private Matcher matcher;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL)
    private List<Answer> answers = new ArrayList<>();

    public Double calculateSimilarity(Integer mostCommonCount) {
        return (((double) mostCommonCount - 1) / ((double) matcher.getGroupSize() - 1)) * answers.size();
    }
}