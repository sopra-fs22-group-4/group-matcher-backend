package ch.uzh.soprafs22.groupmatcher.model.scheduler;

import lombok.Data;

import java.io.Serializable;

@Data
public class ScheduledTaskId implements Serializable {
    private String taskName;
    private String taskInstance;
}
