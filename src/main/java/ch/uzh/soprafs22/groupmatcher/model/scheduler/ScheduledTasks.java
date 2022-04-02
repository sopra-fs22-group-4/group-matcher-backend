package ch.uzh.soprafs22.groupmatcher.model.scheduler;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.sql.Blob;
import java.time.Instant;

@Getter
@Setter
@Entity
@IdClass(ScheduledTaskId.class)
public class ScheduledTasks {

    @Id
    private String taskName;

    @Id
    private String taskInstance;

    @Lob
    private Blob taskData;

    private Instant executionTime;

    private boolean picked;

    private String pickedBy;

    private Instant lastSuccess;

    private Instant lastFailure;

    private Integer consecutiveFailures;

    private Instant lastHeartbeat;

    private Long version;

}
