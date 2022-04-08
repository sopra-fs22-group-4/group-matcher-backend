package ch.uzh.soprafs22.groupmatcher.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
public class MatcherDTO {
    private String name;
    private String description;
    private String courseName;
    private String projectName;
    private ZonedDateTime publishDate;
    private ZonedDateTime dueDate;
    private boolean reminder;
    private Integer groupSize;
    private Integer minGroupSize;
}