package ch.uzh.soprafs22.groupmatcher.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.List;

@Getter
@Setter
@Entity
public class Email {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "matcher_id")
    private Matcher matcher;

    private ZonedDateTime sendAt;

    private boolean sent;

    private String subject = "Subject";

    private String content = "Content";

    public List<String> getRecipients() {
        return matcher.getStudents().stream().map(Student::getEmail).toList();
    }
}
