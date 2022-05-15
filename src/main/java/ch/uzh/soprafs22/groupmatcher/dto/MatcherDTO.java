package ch.uzh.soprafs22.groupmatcher.dto;

import ch.uzh.soprafs22.groupmatcher.constant.MatchingStrategy;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class MatcherDTO {
    private String description;
    private String university;
    private String courseName;
    private ZonedDateTime publishDate;
    private ZonedDateTime dueDate;
    private Integer groupSize;
    private MatchingStrategy matchingStrategy;
    private List<UserDTO> collaborators= new ArrayList<>();
}