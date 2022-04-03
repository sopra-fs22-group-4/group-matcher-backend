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

    @Column(nullable = false)
    private String password;

    private String name;

    @ManyToMany(mappedBy = "students",cascade = CascadeType.ALL)
    private Set<Matcher> matchers  = new LinkedHashSet<>();

    @OneToMany(mappedBy = "student",cascade = CascadeType.ALL)
    private Set<QuestionRes> questionResSet = new LinkedHashSet<>();
}
