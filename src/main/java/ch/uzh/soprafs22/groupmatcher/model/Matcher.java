package ch.uzh.soprafs22.groupmatcher.model;

import ch.uzh.soprafs22.groupmatcher.constant.MatchingStrategy;
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
    private MatchingStrategy matchingStrategy;

    private Integer minGroupSize;

    @OneToMany(mappedBy = "matcher")
    private List<Question> questions = new ArrayList<>();
}
