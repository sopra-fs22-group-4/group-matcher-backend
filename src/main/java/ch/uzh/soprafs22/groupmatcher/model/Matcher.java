package ch.uzh.soprafs22.groupmatcher.model;

import ch.uzh.soprafs22.groupmatcher.constant.MatchingStrategy;
import ch.uzh.soprafs22.groupmatcher.constant.MatcherStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
public class Matcher {
    @Id
    @GeneratedValue
    private Long id;

    private String description;

    private String university;

    private String courseName;

    @CreationTimestamp
    private ZonedDateTime createdAt;

    private ZonedDateTime publishDate;

    private ZonedDateTime dueDate;

    @Enumerated(EnumType.STRING)
    private MatcherStatus status = MatcherStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    private MatchingStrategy matchingStrategy;

    private Integer groupSize;

    @OneToMany(mappedBy = "matcher", cascade = CascadeType.ALL)
    private List<Question> questions = new ArrayList<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "matcher_admins",
            joinColumns = @JoinColumn(name = "matcher_id"),
            inverseJoinColumns = @JoinColumn(name = "admins_id"))
    private List<Admin> collaborators = new ArrayList<>();

    @OneToMany(mappedBy = "matcher", cascade = CascadeType.ALL)
    private List<Student> students = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "matcher", cascade = CascadeType.ALL)
    private List<Team> teams = new ArrayList<>();

    @OneToMany(mappedBy = "matcher", cascade = CascadeType.ALL)
    private List<Notification> notifications = new ArrayList<>();

    public boolean isPublished() {
        return publishDate.isBefore(ZonedDateTime.now());
    }

    public boolean isPastDue() {
        return dueDate.isBefore(ZonedDateTime.now());
    }

    public long getSubmittedCount() {
        return students.stream().filter(student -> student.getSubmissionTimestamp() != null).count();
    }

}