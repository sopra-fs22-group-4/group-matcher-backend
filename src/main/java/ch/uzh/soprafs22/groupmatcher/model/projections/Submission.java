package ch.uzh.soprafs22.groupmatcher.model.projections;

import ch.uzh.soprafs22.groupmatcher.model.Student;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

import java.time.ZonedDateTime;

@Projection(types = {Student.class})
public interface Submission {
    String getEmail();

    String getName();

    ZonedDateTime getSubmissionTimestamp();

    @Value("#{target.matcher.courseName}")
    String getCourseName();
}
