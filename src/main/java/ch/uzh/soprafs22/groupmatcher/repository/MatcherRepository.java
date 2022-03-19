package ch.uzh.soprafs22.groupmatcher.repository;

import ch.uzh.soprafs22.groupmatcher.model.Matcher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MatcherRepository extends JpaRepository<Matcher, Long> {

    Optional<Matcher> findTopByNameContainsOrderByCreatedAtDesc(String partialName);
}
