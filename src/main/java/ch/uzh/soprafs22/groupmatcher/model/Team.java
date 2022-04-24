package ch.uzh.soprafs22.groupmatcher.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Team {
    @Id
    @GeneratedValue
    private Long id;

    private Double similarityScore;

    private Double knowledgeScore;

    private Double skillScore;

    @ManyToOne
    @JoinColumn(name = "matcher_id")
    private Matcher matcher;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL)
    private List<Student> students = new ArrayList<>();

}
