package ch.uzh.soprafs22.groupmatcher.model.projections;

import ch.uzh.soprafs22.groupmatcher.model.Matcher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

import java.time.ZonedDateTime;

@Projection(types = {Matcher.class})
public interface MatcherOverview {
    Long getId();

    String getDescription();

    String getUniversity();

    String getCourseName();

    ZonedDateTime getPublishDate();

    ZonedDateTime getDueDate();

    @Value("#{target.active}")
    boolean isActive();

    @Value("#{@studentRepository.countByMatcher_IdAndSubmissionTimestampNotNullOrderBySubmissionTimestampDesc(target.id)}")
    Integer getSubmissionsCount();
}