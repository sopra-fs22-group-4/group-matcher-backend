package ch.uzh.soprafs22.groupmatcher.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.LinkedHashSet;
import java.util.Set;

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

    @ManyToOne
    @JoinColumn(name = "matcher_id")
    private Matcher matcher;

    @ManyToOne
    @JoinColumn(name = "team_id")
    private Team team;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "student_answers",
            joinColumns = @JoinColumn(name = "student_id"),
            inverseJoinColumns = @JoinColumn(name = "answers_id"))
    private Set<Answer> answers = new LinkedHashSet<>();

}
