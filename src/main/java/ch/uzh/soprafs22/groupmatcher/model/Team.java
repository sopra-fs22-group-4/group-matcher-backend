package ch.uzh.soprafs22.groupmatcher.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class Team {
    @Id
    @GeneratedValue
    private Long id;

    private boolean notified = false;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "matcher_id")
    private Matcher matcher;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL)
    private List<Student> students = new ArrayList<>();

    @ElementCollection
    private List<Double> matchingScores = new ArrayList<>();

    public Team(Student student) {
        this.students.add(student);
    }

    @Override
    public String toString() {
        return students.stream().map(Student::toString).collect(Collectors.joining(", "));
    }


}
