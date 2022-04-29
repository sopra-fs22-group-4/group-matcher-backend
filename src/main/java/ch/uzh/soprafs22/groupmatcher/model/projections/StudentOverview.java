package ch.uzh.soprafs22.groupmatcher.model.projections;

import ch.uzh.soprafs22.groupmatcher.model.Answer;
import ch.uzh.soprafs22.groupmatcher.model.Question;
import ch.uzh.soprafs22.groupmatcher.model.Student;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

import java.time.ZonedDateTime;
import java.util.List;

@Projection(types = {Student.class})
public interface StudentOverview {

    Long getId();

    String getEmail();

    String getName();

    ZonedDateTime getSubmissionTimestamp();

    @Value("#{target.matcher.questions}")
    List<Question> getQuestions();

    List<Answer> getAnswers();
}
