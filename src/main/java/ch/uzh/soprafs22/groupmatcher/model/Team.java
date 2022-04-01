package ch.uzh.soprafs22.groupmatcher.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.List;

@Getter
@Setter
@Entity
public class Team {
    @Id
    @GeneratedValue
    private long id;

    @OneToMany
    @JoinColumn(name = "team_id")
    public List<Student> students;

    private Float similarityScore;

    private Float knowledgeScore;

    private Float skillScore;
}
