package ch.uzh.soprafs22.groupmatcher.model.projections;

import ch.uzh.soprafs22.groupmatcher.model.Matcher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

@Projection(types = {Matcher.class})
public interface MatcherAdminOverview extends MatcherOverview {

    @Value("#{@studentRepository.countByMatcher_IdAndSubmissionTimestampNotNullOrderBySubmissionTimestampDesc(target.id)}")
    Integer getSubmissionsCount();
}
