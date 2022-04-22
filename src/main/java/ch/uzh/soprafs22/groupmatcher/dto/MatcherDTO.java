package ch.uzh.soprafs22.groupmatcher.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
public class MatcherDTO {
    private String description;
    private String university;
    private String semester;
    private String courseName;
    private ZonedDateTime publishDate;
    private ZonedDateTime dueDate;
    private boolean reminder;
    private Integer groupSize;
    private Integer minGroupSize;
}