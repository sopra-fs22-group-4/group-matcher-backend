package ch.uzh.soprafs22.groupmatcher.repository;

import ch.uzh.soprafs22.groupmatcher.model.Matcher;
import ch.uzh.soprafs22.groupmatcher.model.projections.MatcherOverview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatcherRepository extends JpaRepository<Matcher, Long> {

    List<MatcherOverview> findByAdmins_IdOrderByDueDateDesc(Long adminId);
}
