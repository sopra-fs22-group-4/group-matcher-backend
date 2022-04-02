package ch.uzh.soprafs22.groupmatcher.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
public class Team {
    @Id
    @GeneratedValue
    private long id;

    private Float similarityScore;

    private Float knowledgeScore;

    private Float skillScore;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "team_students",
            joinColumns = @JoinColumn(name = "team_id"),
            inverseJoinColumns = @JoinColumn(name = "students_id"))
    private Set<Student> students = new LinkedHashSet<>();

}
