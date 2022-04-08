package ch.uzh.soprafs22.groupmatcher.model;

import ch.uzh.soprafs22.groupmatcher.constant.MatchingStrategy;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity
public class Matcher {
    @Id
    @GeneratedValue
    private Long id;

    private String name;

    private String description;

    private String courseName;

    private String projectName;

    @CreationTimestamp
    private ZonedDateTime createdAt;

    private ZonedDateTime publishDate;

    private ZonedDateTime dueDate;

    private boolean reminder;

    @Enumerated(EnumType.STRING)
    private MatchingStrategy matchingStrategy = MatchingStrategy.MOST_SIMILAR;

    private Integer groupSize;

    private Integer minGroupSize;

    @OneToMany(mappedBy = "matcher", cascade = CascadeType.ALL)
    private List<Question> questions = new ArrayList<>();

    @OneToMany(mappedBy = "matcher", cascade = CascadeType.ALL)
    private List<Email> emails = new ArrayList<>();

    @OneToMany(mappedBy = "matcher", cascade = CascadeType.ALL)
    private Set<QuestionRes> QuestionResSet = new LinkedHashSet<>();

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(name = "matcher_admins",
            joinColumns = @JoinColumn(name = "matcher_id"),
            inverseJoinColumns = @JoinColumn(name = "admins_id"))
    private Set<Admin> admins = new LinkedHashSet<>();

    @OneToMany(mappedBy = "matcher", cascade = CascadeType.ALL)
    private Set<Student> students = new LinkedHashSet<>();

    @OneToMany(mappedBy = "matcher", cascade = CascadeType.ALL)
    private List<Team> teams = new ArrayList<>();

}