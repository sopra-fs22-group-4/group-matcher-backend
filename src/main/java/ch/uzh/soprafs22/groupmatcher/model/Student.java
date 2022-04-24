package ch.uzh.soprafs22.groupmatcher.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
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

    @JsonFormat(pattern = "dd.MM.yyyy")
    private ZonedDateTime submissionTimestamp;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "student_answers",
            joinColumns = @JoinColumn(name = "student_id"),
            inverseJoinColumns = @JoinColumn(name = "answers_id"))
    private Set<Answer> answers = new LinkedHashSet<>();

}
