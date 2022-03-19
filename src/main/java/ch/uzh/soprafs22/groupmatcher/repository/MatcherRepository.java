package ch.uzh.soprafs22.groupmatcher.repository;

import ch.uzh.soprafs22.groupmatcher.model.Matcher;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatcherRepository extends JpaRepository<Matcher, Long> {
}
