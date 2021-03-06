package ch.uzh.soprafs22.groupmatcher.repository;

import ch.uzh.soprafs22.groupmatcher.constant.MatcherStatus;
import ch.uzh.soprafs22.groupmatcher.model.Matcher;
import ch.uzh.soprafs22.groupmatcher.model.projections.MatcherAdminOverview;
import ch.uzh.soprafs22.groupmatcher.model.projections.MatcherOverview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public interface MatcherRepository extends JpaRepository<Matcher, Long> {

    Optional<MatcherOverview> findMatcherById(Long matcherId);

    List<MatcherAdminOverview> findByCollaborators_IdOrderByCreatedAtDesc(Long adminId);

    List<Matcher> findByPublishDateIsBeforeAndStatus(ZonedDateTime sendAt, MatcherStatus status);

    List<Matcher> findByDueDateIsBeforeAndStatus(ZonedDateTime dueAt, MatcherStatus status);

    List<Matcher> findByStatus(MatcherStatus status);
}
