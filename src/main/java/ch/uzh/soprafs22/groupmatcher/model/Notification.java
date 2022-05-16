package ch.uzh.soprafs22.groupmatcher.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.ZonedDateTime;

@Getter
@Setter
@Entity
public class Notification {
    @Id
    @GeneratedValue
    private Long id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "admin_id")
    private Admin creator;

    private String content;

    @CreationTimestamp
    private ZonedDateTime createdAt;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "matcher_id")
    private Matcher matcher;

    @JsonProperty("creatorName")
    public String getCreatorName() {
        return creator.getName();
    }

}
