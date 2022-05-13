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
public class Team {
    @Id
    @GeneratedValue
    private Long id;

    private Double similarityScore;

    private Double knowledgeScore;

    private Double skillScore;

    private boolean notified = false;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "matcher_id")
    private Matcher matcher;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL)
    private List<Student> students = new ArrayList<>();

}
